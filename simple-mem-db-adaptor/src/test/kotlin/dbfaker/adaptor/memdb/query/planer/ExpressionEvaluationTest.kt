package dbfaker.adaptor.memdb.query.planer

import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dbfaker.ResourceId
import dbfaker.adaptor.memdb.DbObject
import dbfaker.adaptor.memdb.json.convert.JsonConverter
import dbfaker.memdb.ObjectValue
import dbfaker.parser.SqlParser
import dbfaker.parser.error.ParseCancellationException
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.lang.invoke.MethodHandles
import java.nio.file.Path
import java.nio.file.Paths


class ExpressionEvaluationTest {
    lateinit var evaluator: ExpressionBuilder

    @Before
    fun setup() {
        val node = jacksonObjectMapper().readTree(readFile("object1.json"))
        val doc = JsonConverter.fromJson(node as ObjectNode)
        evaluator = ExpressionBuilder("c", doc)
    }

    @Test
    fun testSomeSimpleCondition() {
        Assert.assertEquals(true.compareTo(true), 0)
        testSimpleCondition("true", true)
        testSimpleCondition("true and true", true)
        testSimpleCondition("true and false", false)
        testSimpleCondition("false or false", false)
        testSimpleCondition("true or false", true)
        testSimpleCondition("4", 4L)
        testSimpleCondition("4.0", 4L)
        testSimpleCondition("4.3", 4.3)
        testSimpleCondition("4!=5", true)
        testSimpleCondition("3.4<4", true)
        testSimpleCondition("4<3", false)
        testSimpleCondition("4<=4", true)
        testSimpleCondition("4<=3", false)
        testSimpleCondition("5>=3", true)
        testSimpleCondition("6 between 5 and 8", true)
        testSimpleCondition("10 between 5 and 8", false)
        testSimpleCondition("6 not between 5 and 8", false)
        testSimpleCondition("10 not between 5 and 8", true)

        testSimpleCondition("c.id", "db993447-c9cd-48ff-a333-2e8f366dcfc8")
        testSimpleCondition("c.id=\"db993447-c9cd-48ff-a333-2e8f366dcfc8\"", true)
        testSimpleCondition("c.data.trackingOptions.openTrackingEnabled=true", true)
        testSimpleCondition("(c.data.trackingOptions).openTrackingEnabled=true", true)
        testSimpleCondition("c.data.status in(\"DRAFT\",\"PUBLISHING\")", true)
        testSimpleCondition("c.data.status in(\"PUBLISHED\",\"PUBLISHING\")", false)
        testSimpleCondition("c.data.status not in(\"PUBLISHED\",\"PUBLISHING\")", true)
        testSimpleCondition("c.data.status not in(\"DRAFT\",\"PUBLISHING\")", false)
    }

    @Test
    fun testArrayExpressions() {
        testSimpleCondition("[2,3,4,5][1]=3", true)
        testSimpleCondition("[\"abc\",\"bsd\",\"cgh\"][2]=\"cgh\"", true)
        testSimpleCondition("[2,3,4,5]=[2,3,4,5]", true)
    }

    @Test
    fun testObjectExpressions() {
        testSimpleCondition("{a:2,b:3}.b=3", true)
        testSimpleCondition("{a:2,b:3}={a:2,b:3}", true)
    }

    @Test
    fun testFunctionCall() {
        testSimpleCondition("CONTAINS(\"Test307\",\"307\",true)", true)
    }

    @Test(expected = ParseCancellationException::class)
    fun testParsingFailure() {
        testSimpleCondition("a==b", true)
    }

    private fun testSimpleCondition(exprStr: String, expectedValue: Any) {
        val expr = SqlParser.parse("select * from c where $exprStr")
        Assert.assertEquals(expectedValue, evaluator.evaluate(expr.condition!!).value)
    }

    private fun readFile(vararg path: String?): InputStream {
        val url = MethodHandles.lookup().lookupClass().getResource("/")
            ?: throw IllegalArgumentException()
        val folderPath = Paths.get(url.toURI())
        val resPath: Path = Paths.get(folderPath.toString(), *path)
        return FileInputStream(File(resPath.toUri()))
    }
}
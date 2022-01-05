package dbfaker.adaptor.memdb.query.planer

import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dbfaker.ResourceId
import dbfaker.adaptor.memdb.DbObject
import dbfaker.parser.SqlParser
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
        val doc = DbObject.fromJson(node as ObjectNode, ResourceId(123, 234 shl 8))
        evaluator = ExpressionBuilder("c", doc)
    }

    @Test
    fun testSomeSimpleExpr() {
        Assert.assertEquals(true.compareTo(true), 0)
        testSimple("true", true)
        testSimple("true and true", true)
        testSimple("true and false", false)
        testSimple("false or false", false)
        testSimple("true or false", true)
        testSimple("4", 4L)
        testSimple("4.0", 4L)
        testSimple("4.3", 4.3)
        testSimple("4!=5", true)
        testSimple("3.4<4", true)
        testSimple("4<3", false)
        testSimple("4<=4", true)
        testSimple("4<=3", false)
        testSimple("5>=3", true)
        testSimple("6 between 5 and 8", true)
        testSimple("10 between 5 and 8", false)
        testSimple("6 not between 5 and 8", false)
        testSimple("10 not between 5 and 8", true)

        testSimple("c.id", "db993447-c9cd-48ff-a333-2e8f366dcfc8")
        testSimple("c.id=\"db993447-c9cd-48ff-a333-2e8f366dcfc8\"", true)
        testSimple("c.data.trackingOptions.openTrackingEnabled=true", true)
        testSimple("(c.data.trackingOptions).openTrackingEnabled=true", true)
        testSimple("c.data.status in(\"DRAFT\",\"PUBLISHING\")", true)
        testSimple("c.data.status in(\"PUBLISHED\",\"PUBLISHING\")", false)
        testSimple("c.data.status not in(\"PUBLISHED\",\"PUBLISHING\")", true)
        testSimple("c.data.status not in(\"DRAFT\",\"PUBLISHING\")", false)
    }

    @Test
    fun testArrayExpressions() {
        testSimple("[2,3,4,5][1]=3", true)
        testSimple("[\"abc\",\"bsd\",\"cgh\"][2]=\"cgh\"", true)
        testSimple("[2,3,4,5]=[2,3,4,5]", true)
    }

    @Test
    fun testObjectExpressions() {
        testSimple("{a:2,b:3}.b=3", true)
        testSimple("{a:2,b:3}={a:2,b:3}", true)
    }

    @Test
    fun testFunctionCall() {
        testSimple("CONTAINS(\"Test307\",\"307\",true)", true)
    }

    private fun testSimple(exprStr: String, expectedValue: Any) {
        val expr = SqlParser.parse("select * from c where $exprStr")
        Assert.assertEquals(expectedValue, evaluator.evaluate(expr.condition).value)
    }

    private fun readFile(vararg path: String?): InputStream {
        val url = MethodHandles.lookup().lookupClass().getResource("/")
            ?: throw IllegalArgumentException()
        val folderPath = Paths.get(url.toURI())
        val resPath: Path = Paths.get(folderPath.toString(), *path)
        return FileInputStream(File(resPath.toUri()))
    }
}
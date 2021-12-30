package dbfaker.parser

import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.h0tk3y.betterParse.parser.parseToEnd
import dbfaker.JsonDocument
import dbfaker.JsonValue
import dbfaker.planer.ExpressionBuilder
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.lang.invoke.MethodHandles
import java.net.URISyntaxException
import java.nio.file.Path
import java.nio.file.Paths


class ExpressionEvaluationTest {
    lateinit var evaluator: ExpressionBuilder

    @Before
    fun setup() {
        val node = jacksonObjectMapper().readTree(readFile("object1.json"))
        val doc = object : JsonDocument<String>(node as ObjectNode) {
            override val keyClass = String::class.java
        }
        evaluator = ExpressionBuilder("c", doc)
    }

    @Test
    fun testSomeSimpleExpr() {
        testSimple("true", true)
        testSimple("4", 4.0)
        testSimple("4!=5", true)
        testSimple("3<4", true)
        testSimple("4<3", false)
        testSimple("4<=4", true)
        testSimple("4<=3", false)
        testSimple("5>=3", true)
        testSimple("c.id", JsonValue(TextNode("db993447-c9cd-48ff-a333-2e8f366dcfc8")))
        testSimple("c.id=\"db993447-c9cd-48ff-a333-2e8f366dcfc8\"", true)
        testSimple("c.data.trackingOptions.openTrackingEnabled=true", true)
        testSimple("(c.data.trackingOptions).openTrackingEnabled=true", true)
    }

    private fun testSimple(exprStr: String, expectedValue: Any) {
        val expr = SqlGrammar.condition.parseToEnd(SqlGrammar.tokenizer.tokenize(exprStr))
        Assert.assertEquals(expectedValue, evaluator.evaluate(expr))
    }

    private fun readFile(vararg path: String?): InputStream {
        val url = MethodHandles.lookup().lookupClass().getResource("/")
            ?: throw IllegalArgumentException()
        val folderPath = Paths.get(url.toURI())
        val resPath: Path = Paths.get(folderPath.toString(), *path)
        return FileInputStream(File(resPath.toUri()))
    }
}
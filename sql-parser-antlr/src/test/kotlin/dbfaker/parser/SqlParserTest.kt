package dbfaker.parser

import org.junit.Assert
import org.junit.Test

class SqlParserTest {

    @Test
    fun parse() {
        val expr = SqlParser.parse("select * from c where c.id=\"id1\"")
        println(expr)
        Assert.assertNotNull(expr)
    }
}
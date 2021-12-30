package dbfaker.parser

import com.github.h0tk3y.betterParse.grammar.parseToEnd
import org.junit.Assert
import org.junit.Test

class SqlGrammarTest {

    @Test
    fun getRootParser() {
        val expr = SqlGrammar.parseToEnd("select * from c where c.id=\"id1\"")
        println(expr)
        Assert.assertNotNull(expr.toString())
    }
}
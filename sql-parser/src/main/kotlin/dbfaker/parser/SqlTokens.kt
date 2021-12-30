package dbfaker.parser

import com.github.h0tk3y.betterParse.lexer.Token
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import kotlin.reflect.KProperty

internal class LiteralTokenCaseInsensitive(name: String?, val text: String, ignored: Boolean = false) :
    Token(name, ignored) {
    override fun match(input: CharSequence, fromIndex: Int): Int =
        if (input.startsWith(text, fromIndex, true)) text.length else 0

    override fun toString(): String = "${name ?: ""} ($text)" + if (ignored) " [ignorable]" else ""
}

internal fun literalTokenCaseInsensitive(text: String, ignore: Boolean = false): Token {
    return LiteralTokenCaseInsensitive(null, text, ignore)
}

internal class TextLiteralToken(name: String?, ignored: Boolean = false) :
    Token(name, ignored) {
    override fun match(input: CharSequence, fromIndex: Int): Int {
        if (input[fromIndex] != '"') return 0
        var ignoreNext = false;
        for (current in (fromIndex + 1) until input.length) {
            if(ignoreNext)
                ignoreNext = false
            else if (input[current] == '\\')
                ignoreNext = true
            else if (input[current] == '"')
                return current - fromIndex + 1
        }
        return 0;
    }

    override fun toString(): String = (name ?: "") + if (ignored) " [ignorable]" else ""
}

internal fun textLiteralToken(ignore: Boolean = false): Token {
    return TextLiteralToken(null, ignore)
}

internal class SqlTokens() {
    private val _tokens = arrayListOf<Token>()

    val tokens: List<Token> get(): List<Token> = _tokens.distinctBy { it.name ?: it }

    val select by literalTokenCaseInsensitive("select")
    val from by literalTokenCaseInsensitive("from")
    val where by literalTokenCaseInsensitive("where")
    val kTrue by literalToken("true")
    val kFalse by literalToken("false")

    val id by regexToken("[a-zA-Z_][a-zA-Z_0-9]*")
    val number by regexToken("[1-9][0-9]*(\\.[0-9]+)*(e[0-9]+)*")
    val text by textLiteralToken()
    val lpar by literalToken("(")
    val rpar by literalToken(")")
    val comma by literalToken(",")
    val dot by literalToken(".")
    val ne by literalToken("!=")
    val eq by literalToken("=")
    val le by literalToken("<=")
    val ge by literalToken(">=")
    val lt by literalToken("<")
    val gt by literalToken(">")

    val star by literalToken("*")

    val ws by regexToken("\\s+", ignore = true)

    private operator fun Token.provideDelegate(thisRef: SqlTokens, property: KProperty<*>): Token =
        also {
            if (it.name == null) {
                it.name = property.name
            }
            _tokens.add(it)
        }

    private operator fun Token.getValue(thisRef: SqlTokens, property: KProperty<*>): Token = this
}
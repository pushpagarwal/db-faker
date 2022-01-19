package dbfaker.parser

import org.antlr.v4.kotlinruntime.BaseErrorListener
import org.antlr.v4.kotlinruntime.RecognitionException
import org.antlr.v4.kotlinruntime.Recognizer
import dbfaker.parser.error.ParseCancellationException

object ThrowingErrorListener : BaseErrorListener() {

    override fun syntaxError(
        recognizer: Recognizer<*, *>,
        offendingSymbol: Any?,
        line: Int,
        charPositionInLine: Int,
        msg: String,
        e: RecognitionException?
    ) {
        throw ParseCancellationException(
            "Sql Parsing failed at position $line:$charPositionInLine $msg, " +
                    "offending symbol $offendingSymbol"
        )
    }
}
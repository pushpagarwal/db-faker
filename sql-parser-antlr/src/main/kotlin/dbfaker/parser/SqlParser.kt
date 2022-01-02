package dbfaker.parser

import dbfaker.parser.antlr.CosmosDBSqlLexer
import dbfaker.parser.antlr.CosmosDBSqlParser
import org.antlr.v4.kotlinruntime.CharStreams
import org.antlr.v4.kotlinruntime.CommonTokenStream
import dbfaker.parser.antlr.ExpressionVisitor
import dbfaker.parser.model.QueryExpression

object SqlParser {
    fun parse(queryStr: String): QueryExpression {
        val charStream = CharStreams.fromString(queryStr)
        val lexer = CosmosDBSqlLexer(charStream)
        val parser = CosmosDBSqlParser(CommonTokenStream(lexer))
        return ExpressionVisitor().visitRoot(parser.root()) as QueryExpression
    }
}
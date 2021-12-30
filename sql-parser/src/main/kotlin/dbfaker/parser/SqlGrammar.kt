package dbfaker.parser

import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.lexer.*
import com.github.h0tk3y.betterParse.parser.Parser


object SqlGrammar : Grammar<QueryExpression>() {
    private val st = SqlTokens()

    override val tokens: List<Token> get(): List<Token> = st.tokens.distinctBy { it.name ?: it }

    private val bracedExpression by -st.lpar * parser(this::condition) * -st.rpar

    private val constants by
    (st.kFalse map { BooleanConst(false) }) or
            (st.kTrue map { BooleanConst(true) }) or
            (st.text map { TextConst(it.text.substring(1, it.text.length - 1)) }) or
            (st.number map { NumberConst(it.text.toDouble()) })

    private val term: Parser<ScalarExpression> by
    parser(this::idPropertyChain) or
            parser(this::propertyChain) or
            constants

    private val propertyParent: Parser<ScalarExpression> by bracedExpression
    private val property_name: Parser<PropertyExpression> by st.id map { Id(it.text) }
    private val idPropertyChain by leftAssociative(property_name, st.dot) { a, _, b -> IdPropertyPath(a, b) }

    private val propertyChain by ((propertyParent and st.dot and idPropertyChain)
            map { (a, _, b) -> Property(a, b) })


    private val co by
    (st.eq map { EqOp() }) or
            (st.ne map { NeOp() }) or
            (st.lt map { LtOp() }) or
            (st.le map { LeOp() }) or
            (st.gt map { GtOp() }) or
            (st.ge map { GeOp() })


    val condition by leftAssociative(term, co) { a, op, b -> op.compose(a, b) }

    private val query by st.select and st.star and
            st.from and st.id and
            st.where and condition map { QueryExpression(it.t4.text, it.t6)}

    override
    val rootParser by query

}
package dbfaker.parser.antlr

import dbfaker.parser.model.*
import org.antlr.v4.kotlinruntime.ParserRuleContext
import org.antlr.v4.kotlinruntime.tree.TerminalNode

class ExpressionVisitor : CosmosDBSqlParserBaseVisitor<BaseQueryExpression>() {
    private lateinit var aliasList: List<String>
    override fun visitSql_query(ctx: CosmosDBSqlParser.Sql_queryContext): BaseQueryExpression {
        val alias = ctx.findFrom_clause()?.accept(this) as Id
        aliasList = listOf(alias.name)
        val condition = ctx.findWhere_clause()?.accept(this) as ScalarExpression?
        return QueryExpression(alias.name, condition ?: BooleanConst(true))
    }

    override fun visitFrom_clause(ctx: CosmosDBSqlParser.From_clauseContext): BaseQueryExpression {
        return ctx.findFrom_specification()?.accept(this)!!
    }

    override fun visitFrom_specification(ctx: CosmosDBSqlParser.From_specificationContext): BaseQueryExpression {
        if (ctx.JOIN() != null)
            TODO("Join are not supported yet")
        return ctx.findPrimary_from_specification()!!.accept(this)
    }

    override fun visitRelative_path_segment(ctx: CosmosDBSqlParser.Relative_path_segmentContext): BaseQueryExpression {
        return Id(ctx.ID()!!.text)
    }

    override fun visitWhere_clause(ctx: CosmosDBSqlParser.Where_clauseContext): BaseQueryExpression {
        return ctx.findScalar_expression()!!.accept(this)
    }

    override fun visitInputAliasExpression(ctx: CosmosDBSqlParser.InputAliasExpressionContext): BaseQueryExpression {
        val alias = ctx.ID()?.text
        if (alias == null || !aliasList.contains(alias))
            throw IllegalArgumentException("Wrong alias Value")
        return Id(alias)
    }

    override fun visitComparison(ctx: CosmosDBSqlParser.ComparisonContext): BaseQueryExpression {
        val expressions = ctx.findBinary_expression()
        val left = expressions[0].accept(this) as ScalarExpression
        val right = expressions[1].accept(this) as ScalarExpression
        return when (getToken(ctx, compareTypeSet)?.symbol?.type) {
            CosmosDBSqlParser.Tokens.EQUAL.id -> Equality(left, right)
            CosmosDBSqlParser.Tokens.LT.id -> LessThan(left, right)
            CosmosDBSqlParser.Tokens.GT.id -> GreaterThan(left, right)
            CosmosDBSqlParser.Tokens.LE.id -> LessEqual(left, right)
            CosmosDBSqlParser.Tokens.GE.id -> GreaterEqual(left, right)
            CosmosDBSqlParser.Tokens.NOTEQUAL.id -> NonEquality(left, right)
            else -> throw IllegalStateException()
        }
    }

    override fun visitParenthesisScalarExpression(ctx: CosmosDBSqlParser.ParenthesisScalarExpressionContext): BaseQueryExpression {
        return ctx.findScalar_expression()?.accept(this)!!
    }
    override fun visitPropertyPath(ctx: CosmosDBSqlParser.PropertyPathContext): BaseQueryExpression {
        val left = ctx.findPrimary_expression()?.accept(this) as ScalarExpression
        val right = ctx.findProperty_name()?.accept(this) as PropertyExpression
        return when (left) {
            is PropertyExpression -> IdPropertyPath(left, right)
            else -> Property(left, right)
        }
    }

    override fun visitProperty_name(ctx: CosmosDBSqlParser.Property_nameContext): BaseQueryExpression {
        return Id(ctx.text)
    }

    override fun visitConstUndefined(ctx: CosmosDBSqlParser.ConstUndefinedContext): BaseQueryExpression {
        return UndefinedConst
    }

    override fun visitConstFalse(ctx: CosmosDBSqlParser.ConstFalseContext): BaseQueryExpression {
        return BooleanConst(false)
    }

    override fun visitConstTrue(ctx: CosmosDBSqlParser.ConstTrueContext): BaseQueryExpression {
        return BooleanConst(true)
    }

    override fun visitConstNull(ctx: CosmosDBSqlParser.ConstNullContext): BaseQueryExpression {
        return NullConst
    }

    override fun visitConstNumber(ctx: CosmosDBSqlParser.ConstNumberContext): BaseQueryExpression {
        return NumberConst(ctx.NUMBER()!!.text.toDouble())
    }

    override fun visitConstText(ctx: CosmosDBSqlParser.ConstTextContext): BaseQueryExpression {
        val text = ctx.text
        return TextConst(text.substring(1, text.length - 1))
    }


    private fun getToken(ctx: ParserRuleContext, types: Set<Int>): TerminalNode? {
        val children = ctx.children
        if (children == null || children.isEmpty()) {
            return null
        }

        for (node in children) {
            if (node is TerminalNode) {
                val symbol = node.symbol
                if (types.contains(symbol!!.type)) {
                    return node
                }
            }
        }

        return null
    }

    companion object {
        private val compareTypeSet = setOf(
            CosmosDBSqlParser.Tokens.EQUAL.id,
            CosmosDBSqlParser.Tokens.LT.id,
            CosmosDBSqlParser.Tokens.GT.id,
            CosmosDBSqlParser.Tokens.LE.id,
            CosmosDBSqlParser.Tokens.GE.id,
            CosmosDBSqlParser.Tokens.NOTEQUAL.id
        )
    }
}
package dbfaker.parser.antlr

import dbfaker.parser.model.*
import org.antlr.v4.kotlinruntime.ParserRuleContext
import org.antlr.v4.kotlinruntime.tree.TerminalNode
import java.util.stream.Collectors

class ExpressionVisitor : CosmosDBSqlParserBaseVisitor<BaseQueryExpression>() {
    private lateinit var aliasList: List<String>
    override fun visitSql_query(ctx: CosmosDBSqlParser.Sql_queryContext): BaseQueryExpression {
        val alias = ctx.findFrom_clause()?.accept(this) as Id?
        aliasList = if (alias != null) listOf(alias.name) else listOf()
        val condition = ctx.findWhere_clause()?.accept(this) as ScalarExpression?
        val orderByItemList = ctx.findOrderby_clause()?.accept(this) as OrderByItemList?
        val selectExpression = ctx.findSelect_clause()!!.accept(this) as SelectExpression
        return QueryExpression(alias?.name, condition, orderByItemList, selectExpression)
    }

    override fun visitFrom_clause(ctx: CosmosDBSqlParser.From_clauseContext): BaseQueryExpression {
        return ctx.findFrom_specification()?.accept(this)!!
    }

    override fun visitFrom_specification(ctx: CosmosDBSqlParser.From_specificationContext): BaseQueryExpression {
        if (ctx.JOIN() != null)
            TODO("Join are not supported yet")
        if (ctx.findFrom_specification() != null)
            TODO("Nested/complex types are not supported.")
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
            throw IllegalArgumentException("Wrong alias Value.")
        return Id(alias)
    }

    override fun visitBetween_scalar_expression(ctx: CosmosDBSqlParser.Between_scalar_expressionContext)
            : BaseQueryExpression {
        val expressions = ctx.findBinary_expression()
        val first = expressions[0].accept(this) as ScalarExpression
        val second = expressions[1].accept(this) as ScalarExpression
        val third = expressions[2].accept(this) as ScalarExpression
        val ge = GreaterEqual(first, second)
        val le = LessEqual(first, third)
        val expr = And(ge, le)
        return if (ctx.NOT() != null) Not(expr) else expr
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

    override fun visitLogicalANDExpression(ctx: CosmosDBSqlParser.LogicalANDExpressionContext): BaseQueryExpression {
        val expressions = ctx.findLogical_scalar_expression()
        val left = expressions[0].accept(this) as ScalarExpression
        val right = expressions[1].accept(this) as ScalarExpression
        return And(left, right)
    }

    override fun visitLogicalOrExpression(ctx: CosmosDBSqlParser.LogicalOrExpressionContext): BaseQueryExpression {
        val expressions = ctx.findLogical_scalar_expression()
        val left = expressions[0].accept(this) as ScalarExpression
        val right = expressions[1].accept(this) as ScalarExpression
        return Or(left, right)
    }

    override fun visitMultiplication(ctx: CosmosDBSqlParser.MultiplicationContext): BaseQueryExpression {
        val expressions = ctx.findBinary_expression()
        val left = expressions[0].accept(this) as ScalarExpression
        val right = expressions[1].accept(this) as ScalarExpression
        return when (getToken(ctx, multiplicationTypeSet)?.symbol?.type) {
            CosmosDBSqlParser.Tokens.MUL.id -> Multiplication(left, right)
            CosmosDBSqlParser.Tokens.DIV.id -> Divide(left, right)
            CosmosDBSqlParser.Tokens.MOD.id -> ModExpression(left, right)
            else -> throw IllegalStateException()
        }
    }

    override fun visitAddition(ctx: CosmosDBSqlParser.AdditionContext): BaseQueryExpression {
        val expressions = ctx.findBinary_expression()
        val left = expressions[0].accept(this) as ScalarExpression
        val right = expressions[1].accept(this) as ScalarExpression
        return when (getToken(ctx, additionTypeSet)?.symbol?.type) {
            CosmosDBSqlParser.Tokens.ADD.id -> Addition(left, right)
            CosmosDBSqlParser.Tokens.SUB.id -> Subtraction(left, right)
            CosmosDBSqlParser.Tokens.BIT_OR_OP.id -> BitOr(left, right)
            CosmosDBSqlParser.Tokens.BIT_AND_OP.id -> BitAnd(left, right)
            CosmosDBSqlParser.Tokens.BIT_XOR_OP.id -> BitXor(left, right)
            else -> throw IllegalStateException()
        }
    }

    override fun visitUnary_expression(ctx: CosmosDBSqlParser.Unary_expressionContext): BaseQueryExpression {
        val pe = ctx.findPrimary_expression()?.accept(this)!! as ScalarExpression
        return when (ctx.findUnary_operator()?.text) {
            "~" -> BitComplement(pe)
            "NOT" -> Not(pe)
            "-" -> Negate(pe)
            else -> pe
        }

    }

    override fun visitParenthesisScalarExpression(ctx: CosmosDBSqlParser.ParenthesisScalarExpressionContext)
            : BaseQueryExpression {
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

    override fun visitArrayIndexed(ctx: CosmosDBSqlParser.ArrayIndexedContext): BaseQueryExpression {
        val left = ctx.findPrimary_expression()!!.accept(this) as ScalarExpression
        val right = ctx.findScalar_expression()!!.accept(this) as ScalarExpression
        return if (left is PropertyExpression && right is LongConst)
            IdPropertyPath(left, Id(right.value.toString()))
        else
            IndexExpression(left, right)
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

    override fun visitConstInteger(ctx: CosmosDBSqlParser.ConstIntegerContext): BaseQueryExpression {
        return LongConst(ctx.INTEGER()!!.text.toLong())
    }

    override fun visitConstText(ctx: CosmosDBSqlParser.ConstTextContext): BaseQueryExpression {
        val text = ctx.text
        return TextConst(text.substring(1, text.length - 1))
    }

    override fun visitArray_constant(ctx: CosmosDBSqlParser.Array_constantContext): BaseQueryExpression {
        val list = ctx.findArray_constant_list()!!.accept(this) as TemporaryConstExpressionList
        return ArrayConst(list)
    }

    override fun visitArray_constant_list(ctx: CosmosDBSqlParser.Array_constant_listContext): BaseQueryExpression {
        val list: TemporaryConstExpressionList = (ctx.findArray_constant_list()?.accept(this)
            ?: TemporaryConstExpressionList()) as TemporaryConstExpressionList
        val constExpr = ctx.findConstant()?.accept(this) as ConstExpression?
        if (constExpr != null)
            list.items.add(constExpr)
        return list
    }

    override fun visitObject_constant(ctx: CosmosDBSqlParser.Object_constantContext): BaseQueryExpression {
        val list = ctx.findObject_constant_items()!!.accept(this) as TemporaryConstPropertyList
        val map = list.items.stream()
            .collect(Collectors.toUnmodifiableMap({ it.key }, { it.value }))
        return ObjectConst(map)
    }

    override fun visitObject_constant_items(ctx: CosmosDBSqlParser.Object_constant_itemsContext): BaseQueryExpression {
        val list: TemporaryConstPropertyList = (ctx.findObject_constant_items()?.accept(this)
            ?: TemporaryConstPropertyList()) as TemporaryConstPropertyList
        val constProperty = ctx.findObject_constant_item()?.accept(this) as TemporaryConstProperty?
        if (constProperty != null)
            list.items.add(constProperty)
        return list
    }

    override fun visitObject_constant_item(ctx: CosmosDBSqlParser.Object_constant_itemContext): BaseQueryExpression {
        val left = ctx.findProperty_name()!!.accept(this) as Id
        val right = ctx.findConstant()!!.accept(this) as ConstExpression
        return TemporaryConstProperty(left, right)
    }

    override fun visitIn_scalar_expression(ctx: CosmosDBSqlParser.In_scalar_expressionContext): BaseQueryExpression {
        val left = ctx.findBinary_expression()!!.accept(this) as ScalarExpression
        val right = ctx.findIn_scalar_expression_item_list()!!.accept(this) as TemporaryScalarExpressionList
        val expr = InExpression(left, right.items)
        return if (ctx.NOT() != null) Not(expr) else expr
    }

    override fun visitIn_scalar_expression_item_list(ctx: CosmosDBSqlParser.In_scalar_expression_item_listContext)
            : BaseQueryExpression {
        val left: TemporaryScalarExpressionList = (ctx.findIn_scalar_expression_item_list()?.accept(this)
            ?: TemporaryScalarExpressionList()) as TemporaryScalarExpressionList
        val right = ctx.findScalar_expression()?.accept(this) as ScalarExpression?
        if (right != null)
            left.items.add(right)
        return left
    }

    override fun visitArray_create_expression(ctx: CosmosDBSqlParser.Array_create_expressionContext): BaseQueryExpression {
        val expressionList = ctx.findArray_item_list()!!.accept(this) as TemporaryScalarExpressionList
        return ArrayCreation(expressionList)
    }

    override fun visitArray_item_list(ctx: CosmosDBSqlParser.Array_item_listContext): BaseQueryExpression {
        val left: TemporaryScalarExpressionList = (ctx.findArray_item_list()?.accept(this)
            ?: TemporaryScalarExpressionList()) as TemporaryScalarExpressionList
        val right = ctx.findScalar_expression()?.accept(this) as ScalarExpression?
        if (right != null)
            left.items.add(right)
        return left
    }

    override fun visitObject_create_expression(ctx: CosmosDBSqlParser.Object_create_expressionContext): BaseQueryExpression {
        val list = ctx.findObject_property_list()!!.accept(this) as TemporaryScalarPropertyList
        val map = list.items.stream()
            .collect(Collectors.toUnmodifiableMap({ it.key }, { it.value }))
        return ObjectCreation(map)
    }

    override fun visitObject_property_list(ctx: CosmosDBSqlParser.Object_property_listContext): BaseQueryExpression {
        val list: TemporaryScalarPropertyList = (ctx.findObject_property_list()?.accept(this)
            ?: TemporaryConstPropertyList()) as TemporaryScalarPropertyList
        val property = ctx.findObject_property()?.accept(this) as TemporaryScalarProperty?
        if (property != null)
            list.items.add(property)
        return list
    }

    override fun visitObject_property(ctx: CosmosDBSqlParser.Object_propertyContext): BaseQueryExpression {
        val left = ctx.findProperty_name()!!.accept(this) as Id
        val right = ctx.findScalar_expression()!!.accept(this) as ScalarExpression
        return TemporaryScalarProperty(left, right)
    }

    override fun visitFunction_call_expression(ctx: CosmosDBSqlParser.Function_call_expressionContext): BaseQueryExpression {
        if (ctx.K_udf() != null)
            TODO("UDF functions are not supported yet.")
        val name = ctx.findSys_function_name()?.accept(this) as Id
        val arguments = ctx.findFunction_arg_list()?.accept(this) as TemporaryScalarExpressionList?
        return FunctionCall(name, arguments?.items ?: listOf())
    }

    override fun visitFunction_arg_list(ctx: CosmosDBSqlParser.Function_arg_listContext): BaseQueryExpression {
        val left: TemporaryScalarExpressionList = (ctx.findFunction_arg_list()?.accept(this)
            ?: TemporaryScalarExpressionList()) as TemporaryScalarExpressionList
        val right = ctx.findScalar_expression()?.accept(this) as ScalarExpression?
        if (right != null)
            left.items.add(right)
        return left
    }

    override fun visitSys_function_name(ctx: CosmosDBSqlParser.Sys_function_nameContext): BaseQueryExpression {
        return Id(ctx.text)
    }

    override fun visitOrderby_clause(ctx: CosmosDBSqlParser.Orderby_clauseContext): BaseQueryExpression {
        val list = ctx.findOrderby_item_list()!!.accept(this) as TemporaryOrderByItemList
        return OrderByItemList(list)
    }

    override fun visitOrderby_item_list(ctx: CosmosDBSqlParser.Orderby_item_listContext): BaseQueryExpression {
        val left: TemporaryOrderByItemList = (ctx.findOrderby_item_list()?.accept(this)
            ?: TemporaryOrderByItemList()) as TemporaryOrderByItemList
        val right = ctx.findOrderby_item()?.accept(this) as OrderByItem?
        if (right != null)
            left.items.add(right)
        return left
    }

    override fun visitOrderby_item(ctx: CosmosDBSqlParser.Orderby_itemContext): BaseQueryExpression {
        return when (val expr = ctx.findScalar_expression()!!.accept(this)) {
            is PropertyExpression -> OrderByItem(expr, ctx.DESC() != null)
            else -> throw IllegalArgumentException("Only properties are supported as order by item.")
        }
    }

    override fun visitSelect_clause(ctx: CosmosDBSqlParser.Select_clauseContext): BaseQueryExpression {
        if (ctx.findTop_spec() != null)
            TODO("Top queries are not supported yet.")
        return ctx.findSelection()!!.accept(this)
    }

    override fun visitSelection(ctx: CosmosDBSqlParser.SelectionContext): BaseQueryExpression {
        return findSelectList(ctx)
            ?: (ctx.findSelect_value_spec()?.accept(this)
                ?: ctx.MUL()?.let {
                    if (aliasList.isEmpty())
                        throw IllegalArgumentException("Select * is supported with collection only.")
                    else
                        SelectValueExpression(Id(aliasList[0]))
                }
                ?: throw IllegalArgumentException("Invalid select clause."))
    }

    private fun findSelectList(ctx: CosmosDBSqlParser.SelectionContext): SelectItemList? {
        val list = ctx.findSelect_list()?.accept(this) as TemporarySelectItemList?
        return list?.let { SelectItemList(list) }
    }

    override fun visitSelect_value_spec(ctx: CosmosDBSqlParser.Select_value_specContext): BaseQueryExpression {
        val expr = ctx.findScalar_expression()!!.accept(this) as ScalarExpression
        return SelectValueExpression(expr)
    }

    override fun visitSelect_list(ctx: CosmosDBSqlParser.Select_listContext): BaseQueryExpression {
        val left: TemporarySelectItemList = (ctx.findSelect_list()?.accept(this)
            ?: TemporarySelectItemList()) as TemporarySelectItemList
        val right = ctx.findSelect_item()?.accept(this) as SelectItem?
        if (right != null)
            left.items.add(right)
        return left
    }

    override fun visitSelect_item(ctx: CosmosDBSqlParser.Select_itemContext): BaseQueryExpression {
        val expr = ctx.findScalar_expression()!!.accept(this) as ScalarExpression
        val alias = ctx.findSelect_alias()?.accept(this) as Id?
        return SelectItem(expr, alias?.name)
    }

    override fun visitSelect_alias(ctx: CosmosDBSqlParser.Select_aliasContext): BaseQueryExpression {
        return Id(ctx.ID()!!.text)
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


    override fun visitExistsScalarExpression(ctx: CosmosDBSqlParser.ExistsScalarExpressionContext): BaseQueryExpression {
        TODO("Nested Queries are not supported yet.")
    }

    override fun visitArrayScalarExpression(ctx: CosmosDBSqlParser.ArrayScalarExpressionContext): BaseQueryExpression {
        TODO("Nested Queries are not supported yet.")
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
        private val multiplicationTypeSet = setOf(
            CosmosDBSqlParser.Tokens.MUL.id,
            CosmosDBSqlParser.Tokens.DIV.id,
            CosmosDBSqlParser.Tokens.MOD.id,
        )
        private val additionTypeSet = setOf(
            CosmosDBSqlParser.Tokens.ADD.id,
            CosmosDBSqlParser.Tokens.SUB.id,
            CosmosDBSqlParser.Tokens.BIT_AND_OP.id,
            CosmosDBSqlParser.Tokens.BIT_OR_OP.id,
            CosmosDBSqlParser.Tokens.BIT_XOR_OP.id
        )
    }
}
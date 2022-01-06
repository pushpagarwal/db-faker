package dbfaker.adaptor.memdb.query.planer

import dbfaker.adaptor.memdb.DbObject
import dbfaker.memdb.*
import dbfaker.parser.model.*
import java.util.stream.Collectors

typealias QueryPredicate = (DbObject) -> Boolean

typealias SelectTransform = (DbObject) -> BaseJsonValue

class ExpressionBuilder(private val alias: String?, private val doc: DbObject) {

    fun evaluate(expression: ScalarExpression): BaseJsonValue {
        return try {
            when (expression) {
                is NumberConst -> NumberValue.valueOf(expression.value)
                is LongConst -> NumberValue.valueOf(expression.value)
                is TextConst -> TextValue(expression.value)
                is BooleanConst -> BooleanValue.valueOf(expression.value)
                is IdPropertyPath -> doc.at(parsePropertyPath(alias, expression.path))
                is Property -> evaluateProperty(expression)
                is Equality -> compare(expression) { it == 0 }
                is NonEquality -> !(compare(expression) { it == 0 })
                is LessThan -> compare(expression) { it < 0 }
                is GreaterThan -> compare(expression) { it > 0 }
                is LessEqual -> compare(expression) { it <= 0 }
                is GreaterEqual -> compare(expression) { it >= 0 }
                is BitComplement -> performIntExpression(expression) { a -> a.inv() }
                is Negate -> performNumericExpression(expression) { a -> -a }
                is Not -> performNot(expression)
                is Id -> if (alias == expression.name) doc.at("") else doc.get(expression.name)
                NullConst -> NullValue
                UndefinedConst -> UndefinedValue
                is Addition -> performAddition(expression)
                is BitAnd -> performIntExpression(expression) { a, b -> a and b }
                is BitOr -> performIntExpression(expression) { a, b -> a or b }
                is BitXor -> performIntExpression(expression) { a, b -> a xor b }
                is Divide -> performNumericExpression(expression) { a, b -> a / b }
                is ModExpression -> performIntExpression(expression) { a, b -> a % b }
                is Multiplication -> performNumericExpression(expression) { a, b -> a * b }
                is Subtraction -> performNumericExpression(expression) { a, b -> a - b }
                is InExpression -> performInExpression(expression)
                is And -> performBooleanExpression(expression) { a, b -> a && b }
                is Or -> performBooleanExpression(expression) { a, b -> a || b }
                is ArrayConst -> createArray(expression.elements)
                is ObjectConst -> createObject(expression.properties)
                is ArrayCreation -> createArray(expression.elements)
                is ObjectCreation -> createObject(expression.properties)
                is FunctionCall -> functionCall(expression)
                is IndexExpression -> evaluateIndexed(expression)
            }
        } catch (_: Exception) {
            UndefinedValue
        }

    }

    private fun functionCall(expression: FunctionCall): BaseJsonValue {
        val args = expression.argumentList.stream()
            .map { evaluate(it) }
            .collect(Collectors.toList())
        return FunctionResolver.execute(expression.name.name, args)
    }

    private fun compare(expr: BinaryExpression, interpret: (Int) -> Boolean): BooleanValue {
        return BooleanValue.valueOf(
            try {
                val val1 = evaluate(expr.left)
                val val2 = evaluate(expr.right)
                if (!val1.isDefined || !val2.isDefined)
                    false
                else if (val1.type == ValueType.NULL)
                    if (val2.type == ValueType.NULL) interpret.invoke(0) else false
                else if (val1.type != val2.type)
                    false
                else
                    interpret.invoke(val1.compareTo(val2))
            } catch (ex: IllegalArgumentException) {
                false
            }
        )
    }

    private fun performNot(expr: UnaryExpression): BaseJsonValue {
        return when (val v = evaluate(expr.opd)) {
            is BooleanValue -> v.not()
            else -> UndefinedValue
        }
    }

    private fun performBooleanExpression(
        expr: BinaryExpression,
        op: (Boolean, Boolean) -> Boolean
    ): BaseJsonValue {
        val val1 = evaluate(expr.left)
        val val2 = evaluate(expr.right)
        return if (val1 is BooleanValue && val2 is BooleanValue)
            BooleanValue.valueOf(op.invoke(val1.value, val2.value))
        else
            UndefinedValue
    }

    private fun performAddition(expr: BinaryExpression): BaseJsonValue {
        val val1 = evaluate(expr.left)
        val val2 = evaluate(expr.right)
        if (val1.type == ValueType.TEXT && val2.type == ValueType.TEXT)
            return TextValue(val1.value as String + val2.value as String)
        if (val1.type != ValueType.NUMBER && val2.type != ValueType.NUMBER)
            return UndefinedValue
        return NumberValue.valueOf(
            (val1 as NumberValue).value.toDouble() + (val2 as NumberValue).value.toDouble()
        )
    }

    private fun performNumericExpression(expr: BinaryExpression, op: (Double, Double) -> Double): BaseJsonValue {
        val val1 = evaluate(expr.left)
        val val2 = evaluate(expr.right)
        return if (val1 is NumberValue && val2 is NumberValue)
            NumberValue.valueOf(op.invoke(val1.value.toDouble(), val2.value.toDouble()))
        else
            UndefinedValue
    }

    private fun performNumericExpression(expr: UnaryExpression, op: (Double) -> Double): BaseJsonValue {
        val val1 = evaluate(expr.opd)
        return if (val1 is NumberValue)
            NumberValue.valueOf(op.invoke(val1.value.toDouble()))
        else
            UndefinedValue
    }

    private fun performIntExpression(expr: BinaryExpression, op: (Long, Long) -> Long): LongValue {
        val val1 = evaluate(expr.left) as LongValue
        val val2 = evaluate(expr.right) as LongValue
        return NumberValue.valueOf(op.invoke(val1.value, val2.value))
    }

    private fun performIntExpression(expr: UnaryExpression, op: (Long) -> Long): LongValue {
        val val1 = evaluate(expr.opd) as LongValue
        return NumberValue.valueOf(op.invoke(val1.value))
    }

    private fun performInExpression(expr: InExpression): BooleanValue {
        val val1 = evaluate(expr.left)
        val set = expr.right.stream()
            .map { evaluate(it) }
            .collect(Collectors.toSet())
        return BooleanValue.valueOf(set.contains(val1))
    }

    private fun evaluateProperty(property: Property): BaseJsonValue {
        val parentValue = evaluate(property.parent)
        return parentValue.at(property.name.path.replace('.', '/'))
    }

    private fun evaluateIndexed(indexExpression: IndexExpression): BaseJsonValue {
        val parentValue = evaluate(indexExpression.parent)
        val indexValue = evaluate(indexExpression.index)
        return if (indexValue is LongValue)
            parentValue.get(indexValue.value.toInt())
        else
            UndefinedValue
    }

    private fun createArray(elements: List<ScalarExpression>): ArrayValue {
        val elemValues = elements.stream().map { evaluate(it) }.collect(Collectors.toUnmodifiableList())
        return ArrayValue(elemValues)
    }

    private fun createObject(properties: Map<Id, ScalarExpression>): ObjectValue {
        val map = properties.entries.stream().map { Pair(it.key.name, evaluate(it.value)) }
            .collect(Collectors.toUnmodifiableMap({ it.first }, { it.second }))

        return ObjectValue(map)
    }

    companion object {
        fun buildPredicate(alias: String?, expression: ScalarExpression): QueryPredicate {
            return { doc: DbObject ->
                when (val value = ExpressionBuilder(alias, doc).evaluate(expression)) {
                    is BooleanValue -> value.value
                    else -> false
                }
            }
        }

        fun buildOrderBy(alias: String?, orderByItemList: OrderByItemList): Comparator<DbObject> {
            return orderByItemList.elements.stream()
                .map { item -> Pair(parsePropertyPath(alias, item.property.path), item.descending) }
                .map { p ->
                    val cmp = Comparator.comparing<DbObject, BaseJsonValue> { doc -> doc.at(p.first) }
                    if (p.second) cmp.reversed() else cmp
                }
                .reduce { c1, c2 -> c1.thenComparing(c2) }
                .orElseThrow { IllegalStateException() }
        }

        fun buildSelection(alias: String?, selectExpression: SelectExpression): SelectTransform {
            return when (selectExpression) {
                is SelectValueExpression -> { doc -> buildSelectionValue(alias, selectExpression.expression, doc) }
                is SelectItemList -> { doc -> buildSelectionList(alias, selectExpression.elements, doc) }
            }
        }

        private fun buildSelectionValue(
            alias: String?,
            scalarExpression: ScalarExpression,
            doc: DbObject
        ): BaseJsonValue = ExpressionBuilder(alias, doc).evaluate(scalarExpression)


        private fun buildSelectionList(
            alias: String?,
            items: List<SelectItem>,
            doc: DbObject
        ): BaseJsonValue {
            val builder = ExpressionBuilder(alias, doc)
            var index = 0
            val m = items.stream()
                .map {
                    Pair(getName(it.alias, it.expression, ++index), builder.evaluate(it.expression))
                }
                .collect(Collectors.toUnmodifiableMap({ p -> p.first }, { p -> p.second }))
            return ObjectValue(m)
        }

        private fun getName(alias: String?, scalarExpression: ScalarExpression, index: Int): String {
            return alias ?: if (scalarExpression is PropertyExpression) {
                scalarExpression.path.substringAfterLast('.')
            } else
                "\$$index"
        }

        private fun parsePropertyPath(alias: String?, path: String): String {
            val prefix = ".$alias."
            if (alias != null && path.startsWith(prefix)) {
                return path.replaceFirst(".$alias", "").replace('.', '/')
            }
            throw IllegalArgumentException("Illegal path")
        }

    }
}
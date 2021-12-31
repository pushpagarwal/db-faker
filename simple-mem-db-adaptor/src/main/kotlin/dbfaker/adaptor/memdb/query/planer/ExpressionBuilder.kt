package dbfaker.adaptor.memdb.query.planer

import dbfaker.memdb.*
import dbfaker.parser.*

typealias QueryPredicate = (JsonDocument<String>) -> Boolean

class ExpressionBuilder(private val alias: String, private val doc: JsonDocument<String>) {

    fun evaluate(expression: ScalarExpression): JsonValue {
        return when (expression) {
            is NumberConst -> NumberValue(expression.value)
            is TextConst -> TextValue(expression.value)
            is BooleanConst -> BooleanValue.valueOf(expression.value)
            is IdPropertyPath -> doc.at(parsePropertyPath(expression.path))
            is Property -> evaluateProperty(expression)
            is Equality -> compare(expression) { it == 0 }
            is NonEquality -> !(compare(expression) { it == 0 })
            is LessThan -> compare(expression) { it < 0 }
            is GreaterThan -> compare(expression) { it > 0 }
            is LessEqual -> compare(expression) { it <= 0 }
            is GreaterEqual -> compare(expression) { it >= 0 }
            is Id -> doc.get(expression.name)
        }
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

    private fun evaluateProperty(property: Property): JsonValue {
        val parentValue = evaluate(property.parent)
        return parentValue.at(property.name.path.replace('.', '/'))
    }

    private fun parsePropertyPath(path: String): String {
        val prefix = ".$alias."
        if (path.startsWith(prefix)) {
            return path.replaceFirst(".$alias", "").replace('.', '/')
        }
        throw IllegalArgumentException("Illegal path")
    }

    companion object {
        fun build(alias: String, expression: ScalarExpression): QueryPredicate {
            return { doc: JsonDocument<String> ->
                when (val value = ExpressionBuilder(alias, doc).evaluate(expression)) {
                    is BooleanValue -> value.value
                    else -> false
                }
            }

        }

    }
}
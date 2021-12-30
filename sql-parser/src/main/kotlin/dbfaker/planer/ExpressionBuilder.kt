package dbfaker.planer

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dbfaker.Document
import dbfaker.DocumentValue
import dbfaker.JsonValue
import dbfaker.parser.*

typealias QueryPredicate = (Document<String>) -> Boolean

class ExpressionBuilder(private val alias: String, private val doc: Document<String>) {

    fun evaluate(expression: ScalarExpression): Any? {
        return when (expression) {
            is NumberConst -> expression.value
            is TextConst -> expression.value
            is BooleanConst -> expression.value
            is IdPropertyPath -> doc.at(parsePropertyPath(expression.path))
            is Property -> evaluateProperty(expression)
            is Equality -> compare(expression) { it == 0 }
            is NonEquality -> !(compare(expression) { it == 0 })
            is LessThan -> compare(expression) { it < 0 }
            is GreaterThan -> compare(expression) { it > 0 }
            is LessEqual -> compare(expression) { it <= 0 }
            is GreaterEqual -> compare(expression) { it >= 0 }
            else -> null
        }
    }

    private fun compare(expr: BinaryExpression, interpret: (Int) -> Boolean): Boolean {
        return try {
            val val1 = asDocumentValue(evaluate(expr.left))
            val val2 = asDocumentValue(evaluate(expr.right))
            if (val1.type != val2.type)
                false
            else
                interpret.invoke(val1.compareTo(val2))
        } catch (ex: IllegalArgumentException) {
            false
        }
    }

    private fun evaluateProperty(property: Property): DocumentValue? =
        when (val parentValue = evaluate(property.parent)) {
            is DocumentValue -> parentValue.at(property.name.path.replace('.', '/'))
            else -> null
        }

    private fun parsePropertyPath(path: String): String {
        val prefix = ".$alias."
        if (path.startsWith(prefix)) {
            return path.replaceFirst(".$alias", "").replace('.', '/')
        }
        throw IllegalArgumentException("Illegal path")
    }

    private fun asDocumentValue(value: Any?): DocumentValue {
        return when (value) {
            is DocumentValue -> value
            else -> JsonValue(jacksonObjectMapper().valueToTree(value))
        }
    }

    companion object {
        fun build(alias: String, expression: ScalarExpression): QueryPredicate {
            return { doc: Document<String> ->
                when (val value = ExpressionBuilder(alias, doc).evaluate(expression)) {
                    is Boolean -> value
                    else -> false
                }
            }

        }

    }
}
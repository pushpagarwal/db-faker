package dbfaker.adaptor.memdb.query.planer

import dbfaker.adaptor.memdb.DbObject
import dbfaker.memdb.BaseJsonValue
import dbfaker.memdb.BooleanValue
import dbfaker.memdb.ObjectValue
import dbfaker.parser.model.*
import java.util.stream.Collectors


class QueryBuilder private constructor(
    private val query: QueryExpression,
    private val parameters: ObjectValue,
) {

    fun buildPredicate(): QueryPredicate? {
        return query.condition?.let {
            { doc: DbObject ->
                when (val value =
                    ExpressionBuilder(query.alias, doc.root, parameters).evaluate(it)) {
                    is BooleanValue -> value.value
                    else -> false
                }
            }
        }
    }

    fun buildOrderBy(): Comparator<DbObject>? {
        return query.orderBy?.let {
            it.elements.stream()
                .map { item ->
                    Pair(
                        ExpressionBuilder.parsePropertyPath(query.alias, item.property.path),
                        item.descending
                    )
                }
                .map { p ->
                    val cmp = Comparator.comparing<DbObject, BaseJsonValue> { doc -> doc.at(p.first) }
                    if (p.second) cmp.reversed() else cmp
                }
                .reduce { c1, c2 -> c1.thenComparing(c2) }
                .orElseThrow { IllegalStateException() }
        }
    }

    fun buildSelection(): SelectTransform {
        return when (val selectExpression = query.selectExpression) {
            is SelectValueExpression -> { doc ->
                buildSelectionValue(query.alias, selectExpression, doc, parameters)
            }
            is SelectItemList -> { doc ->
                buildSelectionList(
                    query.alias, selectExpression, doc, parameters)
            }
        }
    }

    private fun buildSelectionValue(
        alias: String?,
        expression: SelectValueExpression,
        doc: ObjectValue,
        parameters: ObjectValue
    ): BaseJsonValue = ExpressionBuilder(alias, doc, parameters).evaluate(expression.expression)


    private fun buildSelectionList(
        alias: String?,
        items: SelectItemList,
        doc: ObjectValue,
        parameters: ObjectValue,
    ): BaseJsonValue {
        val builder = ExpressionBuilder(alias, doc, parameters)
        var index = 0
        val m = items.elements.stream()
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

    companion object {
        fun buildQuery(query: QueryExpression, parameters: ObjectValue): Query {
            val builder = QueryBuilder(query, parameters)
            val predicate = builder.buildPredicate()

            val orderBy = builder.buildOrderBy()

            val selectTransform = builder.buildSelection()
            return Query(selectTransform, query.alias, predicate, orderBy)
        }
    }
}
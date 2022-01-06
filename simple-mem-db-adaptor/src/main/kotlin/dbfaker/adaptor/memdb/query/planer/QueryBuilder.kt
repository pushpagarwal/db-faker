package dbfaker.adaptor.memdb.query.planer

import dbfaker.parser.model.QueryExpression


object QueryBuilder {
    fun buildQuery(query: QueryExpression): Query {
        val predicate = query.condition?.let { ExpressionBuilder.buildPredicate(query.alias, it) }

        val orderBy = query.alias?.let { alias ->
            query.orderBy?.let { ExpressionBuilder.buildOrderBy(alias, it) }
        }
        val selectTransform = ExpressionBuilder.buildSelection(query.alias, query.selectExpression)
        return Query(selectTransform, query.alias, predicate, orderBy)
    }
}
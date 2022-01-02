package dbfaker.adaptor.memdb.query.planer

import dbfaker.parser.model.QueryExpression



object QueryBuilder {
    fun buildQuery(query: QueryExpression): Query {
        return Query(ExpressionBuilder.build(query.alias, query.condition))
    }
}
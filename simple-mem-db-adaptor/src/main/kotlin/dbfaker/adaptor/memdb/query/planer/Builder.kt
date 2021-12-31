package dbfaker.adaptor.memdb.query.planer

import dbfaker.parser.QueryExpression



object Builder {
    fun buildQuery(query: QueryExpression): Query {
        return Query(ExpressionBuilder.build(query.alias, query.condition))
    }
}
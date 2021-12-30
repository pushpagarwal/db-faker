package dbfaker.planer

import dbfaker.ScanQuery
import dbfaker.parser.QueryExpression

object QueryPlaner {
    fun planScanQuery(query: QueryExpression): ScanQuery<String> {
        return ScanQuery(ExpressionBuilder.build(query.alias, query.condition))
    }
}
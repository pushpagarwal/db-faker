package dbfaker.adaptor.memdb.query.planer

import dbfaker.adaptor.memdb.DbObject

data class Query(
    val selection: SelectTransform,
    val fromAlias: String?,
    val predicate: QueryPredicate?,
    val orderBy: Comparator<DbObject>?
)

package dbfaker.cosmos.dto

data class Parameter(val name: String, val value: String)

data class QueryRequest(
    val query: String,
    val parameters: List<Parameter>?,
)

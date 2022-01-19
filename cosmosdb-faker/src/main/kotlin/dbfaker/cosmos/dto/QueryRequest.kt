package dbfaker.cosmos.dto

import com.fasterxml.jackson.databind.node.ObjectNode

data class QueryRequest(
    val query: String,
    val parameters: ObjectNode?,
)

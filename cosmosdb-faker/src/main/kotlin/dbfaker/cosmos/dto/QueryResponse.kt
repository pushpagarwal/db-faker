package dbfaker.cosmos.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode

data class QueryResponse(
    @JsonProperty("_rid")
    val rid: String,
    @JsonProperty("_count")
    val count: Int,
    @JsonProperty("Documents")
    val documents: List<JsonNode>
)

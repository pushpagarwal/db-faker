package dbfaker

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface QueryResponseItem {
    val item: JsonNode
    val nextCursor: String
}

data class PartitionKey(
    val paths: List<String>,
    val kind: String
)

interface CosmosCollection {
    val name: String
    val rid: ResourceId
    val parentDbId: String
    val partitionKey: PartitionKey?
    val etag: String
    val ts: Long

    fun getById(id: String): Mono<JsonNode>
    fun upsert(obj: JsonNode): Mono<JsonNode>
    fun updateOnlyIfExist(obj: JsonNode, ifMatch: String? = null): Mono<JsonNode>
    fun query(query: String, params: ObjectNode?, startCursor: String? = null): Flux<QueryResponseItem>
}
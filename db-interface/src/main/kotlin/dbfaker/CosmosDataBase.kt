package dbfaker

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface CosmosDataBase {
    val name: String
    val rid: ResourceId
    val etag: String
    val ts: Long

    fun collections(): Flux<CosmosCollection>
}
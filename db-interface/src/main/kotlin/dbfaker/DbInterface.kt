package dbfaker

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface DbInterface {
    fun databases(): Flux<CosmosDataBase>
    fun queryDatabase(id: String): Mono<CosmosDataBase>
    fun queryContainer(dbId: String, colId: String): Mono<CosmosCollection>
}
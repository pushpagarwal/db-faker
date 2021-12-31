package dbfaker.adaptor.memdb

import dbfaker.CosmosCollection
import dbfaker.CosmosDataBase
import dbfaker.DbInterface
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class SimpleMemDb(private val dbs: List<Database>) : DbInterface {
    override fun databases(): Flux<CosmosDataBase> = Flux.fromIterable(dbs)

    override fun queryDatabase(id: String): Mono<CosmosDataBase> =
        Mono.justOrEmpty(Database.databaseQueryById(dbs, id))

    override fun queryContainer(dbId: String, colId: String): Mono<CosmosCollection> =
        Mono.justOrEmpty(Database.containerQuery(dbs, dbId, colId))

}
package dbfaker.adaptor.memdb

import dbfaker.CosmosCollection
import dbfaker.CosmosDataBase
import dbfaker.ResourceId
import reactor.core.publisher.Flux
import java.time.Instant
import java.util.*

class Database(
    override val name: String,
    override val rid: ResourceId,
    private val _containers: List<DBCollection>,
) : CosmosDataBase {
    override val etag = "\"${UUID.randomUUID()}\""
    override val ts = Instant.now().epochSecond

    override fun collections(): Flux<CosmosCollection> {
        return Flux.fromIterable(_containers)
    }

    fun findContainer(id: String): DBCollection? =
        _containers.stream()
            .filter { c -> c.name == id || c.rid.text == id }
            .findFirst()
            .orElse(null)

    companion object {
        private fun compareById(id: String): (Database) -> Boolean =
            { d -> d.name == id || d.rid.text == id }

        fun databaseQueryById(list: List<Database>, id: String): Database? =
            list.stream()
                .filter(compareById(id))
                .findFirst()
                .orElse(null)

        fun containerQuery(list: List<Database>, dbId: String, colId: String) =
            databaseQueryById(list, dbId)?.findContainer(colId)
    }
}
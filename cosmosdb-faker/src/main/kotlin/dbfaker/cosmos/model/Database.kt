package dbfaker.cosmos.model

import java.time.Instant
import java.util.UUID

class Database(
    val rid: String,
    val name: String,
    val containers: List<DBCollection>,
) {
    val _etag = "\"${UUID.randomUUID()}\""
    val _ts = Instant.now().epochSecond

    fun findContainer(id: String): DBCollection? =
        containers.stream()
            .filter { c -> c.name == id || c.rid.text == id }
            .findFirst()
            .orElse(null)

    companion object {
        private fun compareById(id: String): (Database) -> Boolean =
            { d -> d.name == id || d.rid == id }

        fun databaseQueryById(list: List<Database>): (String) -> Database? =
            { id ->
                list.stream()
                    .filter(compareById(id))
                    .findFirst()
                    .orElse(null)
            }

        fun containerQuery(list: List<Database>): (String, String) -> DBCollection? =
            { dbId, colId -> databaseQueryById(list).invoke(dbId)?.findContainer(colId) }
    }
}
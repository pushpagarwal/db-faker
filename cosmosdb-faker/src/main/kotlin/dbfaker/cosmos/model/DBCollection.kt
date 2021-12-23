package dbfaker.cosmos.model

import dbfaker.Container
import java.time.Instant
import java.util.*

data class PartitionKey(
    val paths: List<String>,
    val kind: String
)

class DBCollection(
    private val c: Container<String, DbObject>,
    val name: String,
    val rid: ResourceId,
    val parentDbId: String,
    val partitionKey: PartitionKey?
) : Container<String, DbObject> by c {
    val _etag = "\"${UUID.randomUUID()}\""
    val _ts = Instant.now().epochSecond
}

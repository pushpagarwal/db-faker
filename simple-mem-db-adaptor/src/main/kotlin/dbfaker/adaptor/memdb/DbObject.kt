package dbfaker.adaptor.memdb

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import dbfaker.ResourceId
import dbfaker.adaptor.memdb.json.convert.JsonConverter
import dbfaker.memdb.BaseJsonValue
import dbfaker.memdb.JsonDocument
import dbfaker.memdb.JsonValue
import dbfaker.memdb.ObjectValue
import java.time.Instant
import java.util.*
import java.util.concurrent.atomic.AtomicLong

class DbObject(private val root: ObjectValue, parentRid: ResourceId) : JsonDocument<String> {
    val _rid: String

    val _self = "/dbs/${parentRid.databaseId}/colls/${parentRid.text}/docs/$id"

    init {
        val rid1 = parentRid.copy(document = dc.incrementAndGet() shl 8)
        _rid = rid1.text
    }

    fun toJson(): JsonNode {
        val newNode = JsonConverter.toJson(root)
        newNode.put("_attachments", "attachments/")
        return newNode
    }

    override val etag: String?
        get() = root.get("_etag").value as String?
    override val id: String
        get() = root.get("id").value as String

    override fun at(path: String): BaseJsonValue = root.at(path)


    override fun get(propertyName: String): BaseJsonValue = root.get(propertyName)

    companion object {
        private val dc: AtomicLong = AtomicLong()

        fun fromJson(node: ObjectNode, parentRid: ResourceId, initState: Boolean = true): DbObject {
            if (initState) {
                if (!node.has("id"))
                    node.put("id", UUID.randomUUID().toString())
                node.put("_ts", Instant.now().epochSecond)
                node.put("_etag", "\"${UUID.randomUUID()}\"")
            }
            val value = JsonConverter.fromJson(node)
            return DbObject(value, parentRid)
        }
    }

}
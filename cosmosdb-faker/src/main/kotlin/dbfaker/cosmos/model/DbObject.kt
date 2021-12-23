package dbfaker.cosmos.model

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import dbfaker.JsonDocument
import java.time.Instant
import java.util.*
import java.util.concurrent.atomic.AtomicLong

class DbObject(node: ObjectNode, parentRid: ResourceId) : JsonDocument(node) {
    val id: String = getId(String::class.java)

    init {
        val rid1 = parentRid.copy(document = dc.incrementAndGet() shl 8)
        node.put("_rid", rid1.text)
        node.put("_self",
            "/dbs/${parentRid.databaseId}/colls/${parentRid.text}/docs/$id")
    }

    fun initState() {
        node.put("_ts", Instant.now().epochSecond)
        node.put("_etag", "\"${UUID.randomUUID().toString()}\"")
    }

    fun toJson(): JsonNode {
        val newNode = node.deepCopy()
        newNode.put("_attachments", "attachments/")
        return newNode
    }

    companion object {
        private val dc: AtomicLong = AtomicLong()
    }
}
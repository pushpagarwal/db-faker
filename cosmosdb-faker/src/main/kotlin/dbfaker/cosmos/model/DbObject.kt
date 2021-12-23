package dbfaker.cosmos.model

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import dbfaker.JsonDocument
import java.time.Instant
import java.util.*
import java.util.concurrent.atomic.AtomicLong

class DbObject(node: ObjectNode, parentRid: ResourceId) : JsonDocument<String>(node) {

    init {
        val rid1 = parentRid.copy(document = dc.incrementAndGet() shl 8)
        node.put("_rid", rid1.text)
        node.put(
            "_self",
            "/dbs/${parentRid.databaseId}/colls/${parentRid.text}/docs/$id"
        )
    }

    fun initState() {
        if (!node.has(id))
            node.put("id", UUID.randomUUID().toString())
        node.put("_ts", Instant.now().epochSecond)
        node.put("_etag", "\"${UUID.randomUUID()}\"")
    }

    fun toJson(): JsonNode {
        val newNode = node.deepCopy()
        newNode.put("_attachments", "attachments/")
        return newNode
    }

    companion object {
        private val dc: AtomicLong = AtomicLong()
    }

    override val keyClass = String::class.java

}
package dbfaker

import com.fasterxml.jackson.core.JsonPointer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.treeToValue
import java.util.*


data class JsonValue(private val node: JsonNode) : DocumentValue {
    override val isObject: Boolean get() = node.isObject

    override val isArray: Boolean get() = node.isArray

    override val isPrimitive: Boolean get() = node.isValueNode

    override fun <T> isCompatible(cls: Class<T>): Boolean {
        TODO("Not yet implemented")
    }

    override fun <T> getAs(cls: Class<T>): T = jacksonObjectMapper().treeToValue(node, cls)
}

abstract class JsonDocument(protected val node: ObjectNode) : Document {
    override val etag: String? get() = node.get("_etag").asText()
    override fun <T> getId(cls: Class<T>): T =
        jacksonObjectMapper().treeToValue(node.get("id"),cls)

    override fun get(path: String): DocumentValue =
        JsonValue(node.at(JsonPointer.valueOf(path)))
}
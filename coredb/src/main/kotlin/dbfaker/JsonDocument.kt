package dbfaker

import com.fasterxml.jackson.core.JsonPointer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.lang.IllegalArgumentException

data class JsonValue(val node: JsonNode) : DocumentValue {
    override val type: ValueType
        get() {
            return if (node.isValueNode) {
                if (node.isNumber)
                    ValueType.NUMBER
                else if (node.isTextual)
                    ValueType.TEXT
                else if (node.isBoolean)
                    ValueType.BOOLEAN
                else
                    ValueType.UNKNOWN

            } else if (node.isArray)
                ValueType.ARRAY
            else if (node.isObject)
                ValueType.OBJECT
            else
                ValueType.UNKNOWN
        }
    override val value: Any
        get() {
            return if (node.isValueNode) {
                if (node.isNumber)
                    node.numberValue().toDouble()
                else if (node.isTextual)
                    node.textValue()
                else if (node.isBoolean)
                    node.booleanValue()
                else
                    Unit
            } else if (node.isArray) {
                listOf(node.elements())
            } else if (node.isObject) {
                val map = mutableMapOf<String, JsonNode>()
                node.fields().forEach { (k, v) -> map[k] = v }
                map
            } else
                Unit
        }

    override fun at(path: String) = JsonValue(node.at(path))

    override fun get(propertyName: String) = JsonValue(node.get(propertyName))

    override fun compareTo(other: DocumentValue): Int {
        val selfType = type
        val otherType = other.type
        if (selfType != otherType)
            throw IllegalArgumentException("Comparison on different type of object.")

        return if (!selfType.isPrimitive || !otherType.isPrimitive) {
            if (value == other.value)
                0
            else throw IllegalArgumentException("Comparison on Non value type.")
        } else {
            when (val v = value) {
                is String -> v.compareTo(other.value as String)
                is Double -> v.compareTo(other.value as Double)
                is Boolean ->
                    if (v == other.value)
                        0
                    else
                        throw IllegalArgumentException("Comparison on Boolean type.")
                else -> throw IllegalArgumentException("Comparison on unknown type of object.")
            }
        }

    }
}

abstract class JsonDocument<T>(protected val node: ObjectNode) : Document<T> {
    override val etag: String? get() = node.get("_etag").asText()
    override val id: T
        get() =
            jacksonObjectMapper().treeToValue(node.get("id"), keyClass)

    override fun get(propertyName: String): DocumentValue =
        JsonValue(node.get(propertyName))

    override fun at(path: String): DocumentValue =
        JsonValue(node.at(JsonPointer.valueOf(path)))

    abstract val keyClass: Class<T>
}
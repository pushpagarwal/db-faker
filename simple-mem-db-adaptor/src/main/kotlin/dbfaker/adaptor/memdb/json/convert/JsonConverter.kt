package dbfaker.adaptor.memdb.json.convert

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.*
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dbfaker.memdb.*
import java.lang.IllegalArgumentException
import java.util.stream.Collectors
import java.util.stream.StreamSupport

object JsonConverter {

    fun fromJson(node: JsonNode): BaseJsonValue {
        if (node.isNull)
            return NullValue
        else if (node.isObject)
            return fromJson(node as ObjectNode)
        else if (node.isArray)
            return fromJson(node as ArrayNode)
        else if (node.isValueNode) {
            if (node.isTextual)
                return TextValue(node.textValue())
            else if (node.isBoolean)
                return BooleanValue.valueOf(node.booleanValue())
            else if (node.isNumber)
                return NumberValue(node.doubleValue())
        }
        throw IllegalArgumentException()
    }

    fun fromJson(node: ObjectNode): ObjectValue {
        val map = mutableMapOf<String, BaseJsonValue>()
        node.fields().forEachRemaining { e -> map[e.key] = fromJson(e.value) }
        return ObjectValue(map)
    }

    fun fromJson(node: ArrayNode): ArrayValue {
        val children = StreamSupport.stream(node.spliterator(), false)
            .map { n -> fromJson(n) }
            .collect(Collectors.toUnmodifiableList())
        return ArrayValue(children)
    }

    fun toJson(value: BaseJsonValue): JsonNode? {
        return when (value) {
            is BooleanValue -> toJson(value)
            is NumberValue -> toJson(value)
            is TextValue -> toJson(value)
            is ArrayValue -> toJson(value)
            is ObjectValue -> toJson(value)
            NullValue -> NullNode.getInstance()
            UndefinedValue -> MissingNode.getInstance()
        }
    }

    fun toJson(value: BooleanValue) = BooleanNode.valueOf(value.value)

    fun toJson(value: NumberValue) = DoubleNode.valueOf(value.value)

    fun toJson(value: TextValue) = TextNode.valueOf(value.value)

    fun toJson(value: ArrayValue): ArrayNode {
        val children = value.value.stream().map { toJson(it) }.collect(Collectors.toList())
        return ArrayNode(jacksonObjectMapper().nodeFactory, children)
    }

    fun toJson(value: ObjectValue): ObjectNode {
        val children = value.value.entries.stream().map { (k, v) -> Pair(k, toJson(v)) }
            .collect(Collectors.toMap({ p -> p.first }, { p -> p.second }))
        return ObjectNode(jacksonObjectMapper().nodeFactory, children)
    }
}
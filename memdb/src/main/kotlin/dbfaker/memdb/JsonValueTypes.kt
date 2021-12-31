package dbfaker.memdb

import com.fasterxml.jackson.core.JsonPointer
import com.fasterxml.jackson.databind.node.ObjectNode

sealed interface BaseJsonValue : JsonValue {
    override val isDefined: Boolean get() = true
    override fun get(propertyName: String): JsonValue = UndefinedValue
    override fun get(index: Int): JsonValue = UndefinedValue
    override fun at(path: String): JsonValue = at(JsonPointer.valueOf(path))
    fun at(ptr: JsonPointer): BaseJsonValue =
        if (ptr.matches()) this
        else internalAt(ptr)?.at(ptr.tail()) ?: UndefinedValue

    fun internalAt(ptr: JsonPointer): BaseJsonValue? = null
}

data class TextValue(override val value: String) : BaseJsonValue {

    override val type: ValueType
        get() = ValueType.TEXT

    override fun compareTo(other: JsonValue): Int {
        val oValue = other.value
        if (oValue is String)
            return value.compareTo(oValue)
        return -1
    }
}

data class NumberValue(override val value: Double) : BaseJsonValue {

    override val type: ValueType
        get() = ValueType.NUMBER

    override fun compareTo(other: JsonValue): Int {
        return when (val oValue = other.value) {
            is Double -> value.compareTo(oValue)
            is String -> 1
            else -> -1
        }
    }
}

data class ArrayValue(override val value: List<BaseJsonValue>) : BaseJsonValue {

    override val type: ValueType
        get() = ValueType.ARRAY

    override fun get(index: Int): JsonValue = value[index]

    override fun internalAt(ptr: JsonPointer): BaseJsonValue? = value[ptr.matchingIndex]

    override fun compareTo(other: JsonValue): Int {
        return when (other) {
            is NullValue, UndefinedValue -> -1
            is ArrayValue ->
                if (value == other.value) 0
                else value.hashCode().compareTo(other.value.hashCode())
            else -> 1
        }
    }
}

data class ObjectValue(override val value: Map<String, BaseJsonValue>) : BaseJsonValue {

    override val type: ValueType
        get() = ValueType.OBJECT

    override fun internalAt(ptr: JsonPointer): BaseJsonValue? = value[ptr.matchingProperty.toString()]

    override fun get(propertyName: String): BaseJsonValue = value[propertyName] ?: UndefinedValue

    override fun compareTo(other: JsonValue): Int {
        return when (other) {
            is ArrayValue, NullValue, UndefinedValue -> -1
            is ObjectNode ->
                if (value == other.value) 0
                else value.hashCode().compareTo(other.value.hashCode())
            else -> 1
        }
    }
}


interface BooleanValue : BaseJsonValue {
    override val value: Boolean

    override val type: ValueType
        get() = ValueType.BOOLEAN

    override fun compareTo(other: JsonValue): Int {
        return if (this === other)
            0
        else when (other.value) {
            false -> -1
            true -> 1
            is Double, String -> 1
            else -> -1
        }
    }

    operator fun not(): BooleanValue =
        if (this === TrueValue) FalseValue else TrueValue

    companion object {
        fun valueOf(v: Boolean) = if (v) TrueValue else FalseValue
    }
}

object TrueValue : BooleanValue {
    override val value: Boolean get() = true
}

object FalseValue : BooleanValue {
    override val value: Boolean get() = false
}


object NullValue : BaseJsonValue {
    override val value: Any? get() = null
    override val type: ValueType get() = ValueType.NULL

    override fun compareTo(other: JsonValue): Int {
        return when (other) {
            is NullValue, UndefinedValue -> 0
            else -> 1
        }
    }

}

object UndefinedValue : BaseJsonValue {
    override val value: Any? get() = null
    override val type: ValueType get() = ValueType.UNDEFINED
    override val isDefined: Boolean get() = false
    override fun compareTo(other: JsonValue): Int {
        return when (other) {
            is NullValue, UndefinedValue -> 0
            else -> 1
        }
    }
}
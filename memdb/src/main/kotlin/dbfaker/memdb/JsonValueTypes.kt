package dbfaker.memdb

import com.fasterxml.jackson.core.JsonPointer
import com.fasterxml.jackson.databind.node.ObjectNode
import java.util.stream.Collectors

sealed interface BaseJsonValue : JsonValue {
    override val isDefined: Boolean get() = true
    override operator fun get(propertyName: String): BaseJsonValue = UndefinedValue
    override fun get(index: Int): BaseJsonValue = UndefinedValue
    override fun at(path: String): BaseJsonValue = at(JsonPointer.valueOf(path))
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

sealed interface NumberValue : BaseJsonValue {
    override val value: Number
    override val type: ValueType get() = ValueType.NUMBER

    override fun compareTo(other: JsonValue): Int {
        val v = this.value
        return when (val oValue = other.value) {
            v -> 0
            is Number -> v.toDouble().compareTo(oValue.toDouble())
            is String -> 1
            else -> -1
        }
    }

    companion object {
        fun valueOf(value: Double): NumberValue {
            if (value.compareTo(value.toLong()) == 0)
                return LongValue(value.toLong())
            return DoubleValue(value)
        }

        fun valueOf(value: Long): LongValue = LongValue(value)
    }
}

data class LongValue(override val value: Long) : NumberValue

data class DoubleValue(override val value: Double) : NumberValue

data class ArrayValue(override val value: List<BaseJsonValue>) : BaseJsonValue {

    override val type: ValueType
        get() = ValueType.ARRAY

    override fun get(index: Int): BaseJsonValue = value[index]

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

    constructor() : this(mapOf())

    override val type: ValueType
        get() = ValueType.OBJECT

    override fun internalAt(ptr: JsonPointer): BaseJsonValue? = value[ptr.matchingProperty.toString()]

    override fun get(propertyName: String): BaseJsonValue = value[propertyName] ?: UndefinedValue

    override fun compareTo(other: JsonValue): Int {
        return when (other) {
            is ArrayValue, NullValue, UndefinedValue -> -1
            is ObjectValue ->
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

fun jsonValueOf(v: String) = TextValue(v)

fun jsonValueOf(v: Long) = NumberValue.valueOf(v)

fun jsonValueOf(v: Double) = NumberValue.valueOf(v)

fun jsonValueOf(v: Boolean) = BooleanValue.valueOf(v)

fun jsonValueOf(v: Any?): BaseJsonValue {
    return when (v) {
        is BaseJsonValue -> v
        is String -> TextValue(v)
        is Number -> NumberValue.valueOf(v.toDouble())
        is Boolean -> BooleanValue.valueOf(v)
        null -> NullValue
        else -> throw IllegalArgumentException("Unsupported value $v")
    }
}

inline fun <reified T> jsonValueOf(list: List<T>) =
    ArrayValue(list.stream().map { jsonValueOf(it) }.collect(Collectors.toList()))
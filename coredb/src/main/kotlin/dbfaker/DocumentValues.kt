package dbfaker

import com.fasterxml.jackson.core.JsonPointer

data class StringValue(override val value: String) : DocumentValue {

    override val type: ValueType
        get() = ValueType.TEXT

    override fun compareTo(other: DocumentValue): Int {
        val oValue = other.value
        if (oValue is String)
            return value.compareTo(oValue)
        return -1
    }
}

data class NumberValue(override val value: Double) : DocumentValue {

    override val type: ValueType
        get() = ValueType.NUMBER

    override fun compareTo(other: DocumentValue): Int {
        return when (val oValue = other.value) {
            is Double -> value.compareTo(oValue)
            is String -> 1
            else -> -1
        }
    }
}

data class BooleanValue(override val value: Boolean) : DocumentValue {

    override val type: ValueType
        get() = ValueType.BOOLEAN

    override fun compareTo(other: DocumentValue): Int {
        return when (val oValue = other.value) {
            is Boolean -> value.compareTo(oValue)
            is Double, String -> 1
            else -> -1
        }
    }
}

data class ArrayValue(override val value: List<DocumentValue>) : DocumentValue {

    override val type: ValueType
        get() = ValueType.ARRAY

    override fun get(index: Int): DocumentValue = value[index]

    override fun internalAt(ptr: JsonPointer): DocumentValue = value[ptr.matchingIndex]

    override fun compareTo(other: DocumentValue): Int {
        return when (val oType = other.type) {
            ValueType.ARRAY ->
                if (value == other.value) 0
                else value.hashCode().compareTo(other.value.hashCode())
            else -> 1
        }
    }
}

data class ObjectValue(override val value: Map<String, DocumentValue>) : DocumentValue {

    override val type: ValueType
        get() = ValueType.OBJECT

    override fun internalAt(ptr: JsonPointer): DocumentValue? = value[ptr.matchingProperty.toString()]

    override fun get(propertyName: String): DocumentValue? = value[propertyName]

    override fun compareTo(other: DocumentValue): Int {
        return when (val oType = other.type) {
            ValueType.ARRAY -> -1
            ValueType.OBJECT -> if (value == other.value) 0
            else value.hashCode().compareTo(other.value.hashCode())
            else -> 1
        }
    }
}
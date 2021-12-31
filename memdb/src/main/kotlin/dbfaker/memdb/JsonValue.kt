package dbfaker.memdb


enum class ValueType {
    NUMBER,
    TEXT,
    BOOLEAN,
    ARRAY,
    OBJECT,
    NULL,
    UNDEFINED;

    val isPrimitive: Boolean get() = this == NUMBER || this == TEXT || this == BOOLEAN
}

interface JsonValue : Comparable<JsonValue> {
    val value: Any?
    val type: ValueType
    val isDefined: Boolean
    fun at(path: String): JsonValue
    fun get(propertyName: String): JsonValue
    fun get(index: Int): JsonValue
}



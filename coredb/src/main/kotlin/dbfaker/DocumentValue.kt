package dbfaker

import com.fasterxml.jackson.core.JsonPointer
import java.lang.IllegalArgumentException

enum class ValueType {
    NUMBER,
    TEXT,
    BOOLEAN,
    ARRAY,
    OBJECT,
    UNKNOWN;

    val isPrimitive: Boolean get() = this == NUMBER || this == TEXT || this == BOOLEAN
}

interface DocumentValue : Comparable<DocumentValue> {
    val value: Any
    val type: ValueType
    fun at(ptr: JsonPointer): DocumentValue? {
        val n: DocumentValue = internalAt(ptr) ?: return null
        return n.at(ptr.tail())
    }

    fun at(path: String) = at(JsonPointer.valueOf(path))
    fun internalAt(ptr: JsonPointer): DocumentValue? = null
    fun get(propertyName: String): DocumentValue? = null
    fun get(index: Int): DocumentValue? = null
}


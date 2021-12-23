package dbfaker

import kotlin.reflect.KClass

interface DocumentValue {
    val isObject: Boolean
    val isArray: Boolean
    val isPrimitive: Boolean
    fun <T> isCompatible(cls:Class<T>) : Boolean
    fun <T> getAs(cls:Class<T>): T
}
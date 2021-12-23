package dbfaker

interface DocumentValue {
    val isObject: Boolean
    val isArray: Boolean
    val isPrimitive: Boolean
    fun <T> isCompatiblePrimitive(cls: Class<T>): Boolean
    fun <T> getAs(cls: Class<T>): T
}
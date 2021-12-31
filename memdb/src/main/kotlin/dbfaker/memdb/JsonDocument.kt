package dbfaker.memdb

interface JsonDocument<T> {
    val etag: String?
    val id: T
    fun at(path: String): JsonValue
    fun get(propertyName: String): JsonValue
}


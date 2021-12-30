package dbfaker

interface Document<T> {
    val etag: String?
    val id: T
    fun at(path: String): DocumentValue
    fun get(propertyName: String): DocumentValue
}


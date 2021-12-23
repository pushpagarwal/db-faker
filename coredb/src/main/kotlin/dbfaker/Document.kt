package dbfaker

interface Document<T> {
    val etag: String?
    val id: T
    fun get(path: String): DocumentValue
}


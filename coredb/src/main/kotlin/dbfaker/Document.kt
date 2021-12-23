package dbfaker

interface Document {
    val etag: String?
    fun <T> getId(cls:Class<T>): T
    fun get(path:String) : DocumentValue
}
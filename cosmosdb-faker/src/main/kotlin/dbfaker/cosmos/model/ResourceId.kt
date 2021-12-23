package dbfaker.cosmos.model

import java.nio.ByteBuffer
import java.util.*

data class ResourceId(
    val database: Int = 0,
    val documentCollection: Int = 0,
    val document: Long = 0L,
    val offer: Int = 0,
) {
    val text: String by lazy { calcText() }

    val databaseId: String by lazy { ResourceId(database = database).text }

    val collectionId: String by lazy {
        ResourceId(database = database, documentCollection = documentCollection).text
    }

    private fun calcText(): String {
        var len = 0
        if (offer != 0)
            len += 3
        else if (database != 0)
            len += Integer.BYTES
        if (documentCollection != 0)
            len += Integer.BYTES
        if (document != 0L)
            len += Long.SIZE_BYTES
        val buffer = ByteBuffer.allocate(len)
        var index = 0
        if (offer != 0) {
            buffer.putShort(index, ((offer and 0x00FFFF00) shr 8).toShort())
            buffer.put(index, (offer and 0xff).toByte())
            index += Integer.BYTES
        } else if (database != 0) {
            buffer.putInt(index, database)
            index += Integer.BYTES
        }
        if (documentCollection != 0) {
            buffer.putInt(index, documentCollection)
            index += Integer.BYTES
        }
        if (document != 0L) {
            buffer.putLong(index, document)
            index += Long.SIZE_BYTES
        }
        return Base64.getEncoder().encodeToString(buffer.array()).replace('/', '-')
    }
}

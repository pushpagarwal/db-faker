package dbfaker.memdb

import dbfaker.memdb.exceptions.NotFound
import dbfaker.memdb.exceptions.PreConditionFailed



class InMemoryContainer<K, V : JsonDocument<K>>(private val cls: Class<K>) {
    private val index = mutableMapOf<K, V>()

    fun getById(id: K): V? {
        return index[id]
    }

    fun upsert(obj: V): V {
        val id = obj.id
        return update(id, obj)
    }

    fun updateOnlyIfExist(obj: V, ifMatch: String?): V {
        val id = obj.id
        if (index.contains(id)) {
            if (ifMatch != null && ifMatch != index[id]!!.etag)
                throw PreConditionFailed("Etag doesn't match.")
            return update(id, obj)
        } else
            throw NotFound()
    }

    fun insertOnlyIfNotExist(obj: V): V {
        val id = obj.id
        return if (!index.contains(id)) {
            update(id, obj)
        } else
            throw PreConditionFailed("Object with id exists")
    }


    private fun update(id: K, obj: V): V {
        index[id] = obj
        return index[id]!!
    }

}
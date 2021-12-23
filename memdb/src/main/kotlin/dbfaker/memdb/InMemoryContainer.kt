package dbfaker.memdb

import dbfaker.Container
import dbfaker.Document
import exceptions.NotFound
import exceptions.PreConditionFailed
import reactor.core.publisher.Mono


class InMemoryContainer<K,V: Document>(private val cls: Class<K>) : Container<K,V> {
    private val index = mutableMapOf<K, V>()

    override fun getById(id: K): Mono<V> {
        return Mono.justOrEmpty(index[id])
    }

    override fun upsert(obj: V): Mono<V> {
        val id = getId(obj)
        return Mono.just(update(id, obj))
    }

    override fun updateOnlyIfExist(obj: V, ifMatch:String?): Mono<V> {
        val id = getId(obj)
        if (index.contains(id)) {
            if(ifMatch != null
                && ifMatch != index[id]!!.etag)
                throw PreConditionFailed("Etag doesn't match.")
            return Mono.just(update(id, obj))
        } else
            return Mono.error(NotFound())
    }

    override fun insertOnlyIfNotExist(obj: V): Mono<V> {
        val id = getId(obj)
        return if (!index.contains(id)) {
            Mono.just(update(id, obj))
        } else
            Mono.error(PreConditionFailed("Object with id exists"))
    }
    private fun update(id: K, obj: V): V {
        index[id] = obj
        return index[id]!!
    }

    private fun getId(obj: V): K = obj.getId(cls)
}
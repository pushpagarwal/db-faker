package dbfaker.memdb

import dbfaker.Container
import dbfaker.Document
import dbfaker.ScanQuery
import exceptions.NotFound
import exceptions.PreConditionFailed
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono


class InMemoryContainer<K, V : Document<K>>(private val cls: Class<K>) : Container<K, V> {
    private val index = mutableMapOf<K, V>()

    override fun getById(id: K): Mono<V> {
        return Mono.justOrEmpty(index[id])
    }

    override fun upsert(obj: V): Mono<V> {
        val id = obj.id
        return Mono.just(update(id, obj))
    }

    override fun updateOnlyIfExist(obj: V, ifMatch: String?): Mono<V> {
        val id = obj.id
        if (index.contains(id)) {
            if (ifMatch != null && ifMatch != index[id]!!.etag)
                return Mono.error(PreConditionFailed("Etag doesn't match."))
            return Mono.just(update(id, obj))
        } else
            return Mono.error(NotFound())
    }

    override fun insertOnlyIfNotExist(obj: V): Mono<V> {
        val id = obj.id
        return if (!index.contains(id)) {
            Mono.just(update(id, obj))
        } else
            Mono.error(PreConditionFailed("Object with id exists"))
    }

    override fun scanQuery(query: ScanQuery<K>, startCursor: String, limit: Int): Flux<V> {
        return Flux.empty()
    }


    private fun update(id: K, obj: V): V {
        index[id] = obj
        return index[id]!!
    }

}
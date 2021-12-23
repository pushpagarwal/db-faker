package dbfaker

import reactor.core.publisher.Mono

interface Container<K,V:Document> {
    fun getById(id:K): Mono<V>
    fun upsert(obj: V): Mono<V>
    fun updateOnlyIfExist(obj: V, ifMatch:String?= null): Mono<V>
    fun insertOnlyIfNotExist(obj: V): Mono<V>
}
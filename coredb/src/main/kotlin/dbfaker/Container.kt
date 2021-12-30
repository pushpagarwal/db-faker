package dbfaker

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.function.Predicate

data class ScanQuery<K>(val predicate: Predicate<Document<K>>)

interface Container<K, V : Document<K>> {
    fun getById(id: K): Mono<V>
    fun upsert(obj: V): Mono<V>
    fun updateOnlyIfExist(obj: V, ifMatch: String? = null): Mono<V>
    fun insertOnlyIfNotExist(obj: V): Mono<V>
    fun scanQuery(query: ScanQuery<K>, startCursor: String, limit: Int): Flux<V>
}
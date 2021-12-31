package dbfaker.adaptor.memdb

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import dbfaker.*
import dbfaker.adaptor.memdb.query.planer.QueryBuilder
import dbfaker.memdb.InMemoryContainer
import dbfaker.parser.SqlGrammar
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.*

class DBCollection(
    private val c: InMemoryContainer<String, DbObject>,
    override val name: String,
    override val rid: ResourceId,
    override val parentDbId: String,
    override val partitionKey: PartitionKey?
) : CosmosCollection {
    override val etag = "\"${UUID.randomUUID()}\""
    override val ts = Instant.now().epochSecond
    override fun getById(id: String): Mono<JsonNode> = Mono.justOrEmpty(c.getById(id)?.toJson())


    override fun upsert(obj: JsonNode): Mono<JsonNode> =
        Mono.justOrEmpty(c.upsert(DbObject.fromJson(obj as ObjectNode, rid)).toJson())


    override fun updateOnlyIfExist(obj: JsonNode, ifMatch: String?): Mono<JsonNode> =
        Mono.justOrEmpty(
            c.updateOnlyIfExist(DbObject.fromJson(obj as ObjectNode, rid, false), ifMatch)
                .toJson()
        )

    override fun query(query: String, startCursor: String?): Flux<QueryResponseItem> {
        val queryExpression = SqlGrammar.parseToEnd(query)
        val q = QueryBuilder.buildQuery(queryExpression)
        return Flux.fromStream(
            c.stream()
                .filter(q.predicate)
                .map { DbQueryResponseItem(it.toJson(), "") }
        )
    }
}

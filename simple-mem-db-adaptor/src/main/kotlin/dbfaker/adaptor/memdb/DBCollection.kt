package dbfaker.adaptor.memdb

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import dbfaker.CosmosCollection
import dbfaker.PartitionKey
import dbfaker.QueryResponseItem
import dbfaker.ResourceId
import dbfaker.adaptor.memdb.json.convert.JsonConverter
import dbfaker.adaptor.memdb.query.planer.QueryBuilder
import dbfaker.memdb.InMemoryContainer
import dbfaker.memdb.ObjectValue
import dbfaker.memdb.UndefinedValue
import dbfaker.parser.SqlParser
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
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

    fun upsert(obj: DbObject) = c.upsert(obj)

    override fun updateOnlyIfExist(obj: JsonNode, ifMatch: String?): Mono<JsonNode> =
        Mono.justOrEmpty(
            c.updateOnlyIfExist(DbObject.fromJson(obj as ObjectNode, rid, false), ifMatch)
                .toJson()
        )

    override fun query(query: String, params: ObjectNode?, startCursor: String?): Flux<QueryResponseItem> {
        val queryExpression = SqlParser.parse(query)
        val q = QueryBuilder.buildQuery(queryExpression,
            params?.let { JsonConverter.fromJson(it) } ?: ObjectValue()
        )
        return if (q.fromAlias != null) {
            var stream = c.stream()
            stream = q.orderBy?.let { stream.sorted(q.orderBy) } ?: stream
            stream = q.predicate?.let { stream.filter(q.predicate) } ?: stream
            Flux.fromStream(stream)
                .subscribeOn(Schedulers.parallel())
                .map { it.root }
                .map(q.selection)
                .filter { it != UndefinedValue }
        } else {
            Flux.just(q.selection.invoke(ObjectValue()))
        }.map { DbQueryResponseItem(JsonConverter.toJson(it), "") }
    }
}

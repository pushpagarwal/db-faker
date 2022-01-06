package dbfaker.cosmos.handlers

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import dbfaker.DbInterface
import dbfaker.QueryResponseItem
import dbfaker.cosmos.dto.QueryRequest
import dbfaker.cosmos.dto.QueryResponse
import dbfaker.memdb.exceptions.NotFound
import dbfaker.memdb.exceptions.PreConditionFailed
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

@Component
class DbDocumentHandler(private val dbInterface: DbInterface) {

    fun getById(serverRequest: ServerRequest): Mono<ServerResponse> {
        val dbId = serverRequest.pathVariable("dbId")
        val colId = serverRequest.pathVariable("colId")
        val docId = serverRequest.pathVariable("docId")
        val mono = dbInterface.queryContainer(dbId, colId)
            .flatMap { container -> container.getById(docId) }

        return handleDbCollectionResponse(mono)
    }

    fun create(serverRequest: ServerRequest): Mono<ServerResponse> {
        val dbId = serverRequest.pathVariable("dbId")
        val colId = serverRequest.pathVariable("colId")
        val jsonMono = serverRequest.bodyToMono(JsonNode::class.java)
        val containerMono = dbInterface.queryContainer(dbId, colId)
        val mono = Mono.zip(jsonMono, containerMono)
            .flatMap { t -> t.t2.upsert(t.t1) }
        return handleDbCollectionResponse(mono)
    }

    fun updateById(serverRequest: ServerRequest): Mono<ServerResponse> {
        val dbId = serverRequest.pathVariable("dbId")
        val colId = serverRequest.pathVariable("colId")
        val docId = serverRequest.pathVariable("docId")
        val ifMatch = serverRequest.headers().firstHeader(HttpHeaders.IF_MATCH)
        val jsonMono = serverRequest.bodyToMono(JsonNode::class.java)
            .filter { node ->
                node.has("id") && node.get("id").isTextual && node.get("id").textValue() == docId
            }

        val containerMono = dbInterface.queryContainer(dbId, colId)
        val mono = Mono.zip(jsonMono, containerMono)
            .flatMap { t -> t.t2.updateOnlyIfExist(t.t1, ifMatch) }
        return handleDbCollectionResponse(mono)
    }

    fun query(serverRequest: ServerRequest): Mono<ServerResponse> {
        val dbId = serverRequest.pathVariable("dbId")
        val colId = serverRequest.pathVariable("colId")
        val jsonMono = serverRequest.bodyToMono(QueryRequest::class.java)
        val containerMono = dbInterface.queryContainer(dbId, colId)
        val mono = Mono.zip(jsonMono, containerMono)
            .flatMapMany { t -> t.t2.query(t.t1.query) }
            .map(QueryResponseItem::item)
            .collectList()
            .map { list -> QueryResponse("", list.size, list) }
        return handleDbCollectionResponse(mono)
    }

    fun <T : Any> handleDbCollectionResponse(dbObjectMono: Mono<T>): Mono<ServerResponse> {
        return dbObjectMono
            .flatMap { v -> ServerResponse.ok().bodyValue(v) }
            .onErrorResume(NotFound::class.java) { ServerResponse.notFound().build() }
            .onErrorResume(PreConditionFailed::class.java) { ServerResponse.status(409).build() }
    }


}
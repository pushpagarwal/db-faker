package dbfaker.cosmos.handlers

import com.fasterxml.jackson.databind.JsonNode
import dbfaker.DbInterface
import dbfaker.QueryResponseItem
import dbfaker.cosmos.dto.QueryRequest
import dbfaker.cosmos.dto.QueryResponse
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
        return dbInterface.queryContainer(dbId, colId)
            .flatMap { container -> container.getById(docId) }
            .flatMap { v -> ServerResponse.ok().bodyValue(v) }

    }

    fun create(serverRequest: ServerRequest): Mono<ServerResponse> {
        val dbId = serverRequest.pathVariable("dbId")
        val colId = serverRequest.pathVariable("colId")
        val jsonMono = serverRequest.bodyToMono(JsonNode::class.java)
        val containerMono = dbInterface.queryContainer(dbId, colId)
        return Mono.zip(jsonMono, containerMono)
            .flatMap { t -> t.t2.upsert(t.t1) }
            .flatMap { v -> ServerResponse.ok().bodyValue(v) }
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
        return Mono.zip(jsonMono, containerMono)
            .flatMap { t -> t.t2.updateOnlyIfExist(t.t1, ifMatch) }
            .flatMap { v -> ServerResponse.ok().bodyValue(v) }

    }

    fun query(serverRequest: ServerRequest): Mono<ServerResponse> {
        val dbId = serverRequest.pathVariable("dbId")
        val colId = serverRequest.pathVariable("colId")
        val jsonMono = serverRequest.bodyToMono(QueryRequest::class.java)
        val containerMono = dbInterface.queryContainer(dbId, colId)
        return Mono.zip(jsonMono, containerMono)
            .flatMapMany { t -> t.t2.query(t.t1.query) }
            .map(QueryResponseItem::item)
            .collectList()
            .map { list -> QueryResponse("", list.size, list) }
            .flatMap { v -> ServerResponse.ok().bodyValue(v) }
    }


}
package dbfaker.cosmos.handlers

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import dbfaker.DbInterface
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

    fun handleDbCollectionResponse(dbObjectMono: Mono<JsonNode>): Mono<ServerResponse> {
        return dbObjectMono
            .flatMap(ServerResponse.ok()::bodyValue)
            .onErrorResume(NotFound::class.java) { ServerResponse.notFound().build() }
            .onErrorResume(PreConditionFailed::class.java) { ServerResponse.status(409).build() }
    }


}
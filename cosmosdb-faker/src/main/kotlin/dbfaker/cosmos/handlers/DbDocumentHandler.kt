package dbfaker.cosmos.handlers

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import dbfaker.cosmos.model.Database
import dbfaker.cosmos.model.DbObject
import exceptions.NotFound
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

@Component
class DbDocumentHandler(databases: List<Database>) {
    private val queryContainer = Database.containerQuery(databases)

    fun getById(serverRequest: ServerRequest): Mono<ServerResponse> {
        val dbId = serverRequest.pathVariable("dbId")
        val colId = serverRequest.pathVariable("colId")
        val docId = serverRequest.pathVariable("docId")
        val container = queryContainer(dbId, colId) ?: return ServerResponse.notFound().build()
        return handleDbCollectionResponse(
            container.getById(docId)
                .switchIfEmpty(Mono.error(NotFound()))
        )
    }

    fun updateById(serverRequest: ServerRequest): Mono<ServerResponse> {
        val dbId = serverRequest.pathVariable("dbId")
        val colId = serverRequest.pathVariable("colId")
        val docId = serverRequest.pathVariable("docId")
        val container = queryContainer(dbId, colId) ?: return ServerResponse.notFound().build()
        val ifMatch = serverRequest.headers().header(HttpHeaders.IF_MATCH).first()
        return serverRequest.bodyToMono(JsonNode::class.java)
            .filter { node ->
                node.has("id") && node.get("id").isTextual && node.get("id").textValue() == docId
            }
            .flatMap { node ->
                val obj = DbObject(node as ObjectNode, container.rid)
                obj.initState()
                handleDbCollectionResponse(container.updateOnlyIfExist(obj, ifMatch))
            }
            .switchIfEmpty(ServerResponse.badRequest().build())
    }

    fun handleDbCollectionResponse(dbObjectMono: Mono<DbObject>): Mono<ServerResponse> {
        return dbObjectMono.map(DbObject::toJson)
            .flatMap(ServerResponse.ok()::bodyValue)
            .onErrorResume(NotFound::class.java) { ServerResponse.notFound().build() }
    }


}
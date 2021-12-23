package dbfaker.cosmos.handlers

import dbfaker.cosmos.model.Database
import dbfaker.cosmos.model.DbObject
import exceptions.NotFound
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

@Component
class DbDocumentHandler(private val databases: List<Database>) {
    private val queryContainer = Database.containerQuery(databases)

    fun getById(serverRequest: ServerRequest): Mono<ServerResponse> {
        val dbId = serverRequest.pathVariable("dbId")
        val colId = serverRequest.pathVariable("colId")
        val docId = serverRequest.pathVariable("docId")
        val container = queryContainer(dbId, colId) ?: return ServerResponse.notFound().build()
        return container.getById(docId).map(DbObject::toJson)
            .flatMap(ServerResponse.ok()::bodyValue)
            .switchIfEmpty(Mono.error(NotFound()))
            .onErrorResume(NotFound::class.java) { ServerResponse.notFound().build() }
    }
}
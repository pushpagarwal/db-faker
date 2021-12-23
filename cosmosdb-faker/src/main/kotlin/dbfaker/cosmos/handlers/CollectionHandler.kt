package dbfaker.cosmos.handlers

import dbfaker.cosmos.dto.CollectionMapper
import dbfaker.cosmos.model.DBCollection
import dbfaker.cosmos.model.Database
import org.mapstruct.factory.Mappers
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import java.util.*
import java.util.stream.Collectors
import kotlin.streams.toList

@Component
class CollectionHandler(private val databases: List<Database>) {
    private val queryContainer = Database.containerQuery(databases)
    private val queryDb = Database.databaseQueryById(databases)

    fun list(serverRequest: ServerRequest): Mono<ServerResponse>{
        val dbId = serverRequest.pathVariable("dbId")
        val containers = (queryDb(dbId)?.containers?:listOf()).stream()
            .map { c -> Mappers.getMapper(CollectionMapper::class.java).toDto(c) }
            .collect(Collectors.toUnmodifiableList())
        return ServerResponse.ok().bodyValue(containers)
    }

    fun getById(serverRequest: ServerRequest): Mono<ServerResponse>{
        val dbId = serverRequest.pathVariable("dbId")
        val colId = serverRequest.pathVariable("colId")
        val container = queryContainer(dbId, colId) ?: return ServerResponse.notFound().build()
        val dto = Mappers.getMapper(CollectionMapper::class.java).toDto(container)
        return ServerResponse.ok().bodyValue(dto)

    }
}
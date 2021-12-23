package dbfaker.cosmos.handlers

import dbfaker.cosmos.dto.DatabaseDto
import dbfaker.cosmos.dto.DatabaseMapper
import dbfaker.cosmos.dto.DatabasesDto
import dbfaker.cosmos.dto.RootDto
import dbfaker.cosmos.model.Database
import exceptions.NotFound
import org.mapstruct.factory.Mappers
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import java.util.stream.Collectors

@Component
class DbHandler(private val databases: List<Database>) {
    val queryDb = Database.databaseQueryById(databases)

    fun root(serverRequest: ServerRequest): Mono<ServerResponse> {
        return ServerResponse.ok().bodyValue(RootDto())
    }

    fun listDatabases(serverRequest: ServerRequest): Mono<ServerResponse> {
        val dto = databases.stream()
            .map { d-> Mappers.getMapper(DatabaseMapper::class.java).toDto(d)!! }
            .collect(Collectors.toList())
        return ServerResponse.ok().bodyValue(DatabasesDto(dto))
    }

    fun getDatabase(serverRequest: ServerRequest): Mono<ServerResponse> {
        val id = serverRequest.pathVariable("id")
        val db = queryDb(id)
        val dto = Mappers.getMapper(DatabaseMapper::class.java).toDto(db)
            ?: return ServerResponse.notFound().build()
        return ServerResponse.ok()
            .header("etag", dto._etag)
            .bodyValue(dto)
    }

}
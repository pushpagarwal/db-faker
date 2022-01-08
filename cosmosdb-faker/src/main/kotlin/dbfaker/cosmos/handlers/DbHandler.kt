package dbfaker.cosmos.handlers

import dbfaker.DbInterface
import dbfaker.cosmos.dto.DatabaseMapper
import dbfaker.cosmos.dto.DatabasesDto
import dbfaker.cosmos.dto.RootDto
import dbfaker.memdb.exceptions.NotFound
import org.mapstruct.factory.Mappers
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import java.util.stream.Collectors

@Component
class DbHandler(private val dbInterface: DbInterface) {

    fun root(serverRequest: ServerRequest): Mono<ServerResponse> {
        return ServerResponse.ok().bodyValue(RootDto())
    }

    fun listDatabases(serverRequest: ServerRequest): Mono<ServerResponse> {
        return dbInterface.databases()
            .map { d -> Mappers.getMapper(DatabaseMapper::class.java).toDto(d) }
            .collect(Collectors.toList())
            .flatMap { dto -> ServerResponse.ok().bodyValue(DatabasesDto(dto)) }
    }

    fun getDatabase(serverRequest: ServerRequest): Mono<ServerResponse> {
        val id = serverRequest.pathVariable("id")
        return dbInterface.queryDatabase(id)
            .map { db -> Mappers.getMapper(DatabaseMapper::class.java).toDto(db) }
            .flatMap { dto ->
                ServerResponse.ok()
                    .header("etag", dto!!._etag)
                    .bodyValue(dto)
            }
            .switchIfEmpty(Mono.error(NotFound()))
    }

}

package dbfaker.cosmos.handlers

import dbfaker.CosmosDataBase
import dbfaker.DbInterface
import dbfaker.cosmos.dto.CollectionMapper
import org.mapstruct.factory.Mappers
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import java.util.stream.Collectors

@Component
class CollectionHandler(private val dbInterface: DbInterface) {

    fun list(serverRequest: ServerRequest): Mono<ServerResponse> {
        val dbId = serverRequest.pathVariable("dbId")
        return dbInterface.queryDatabase(dbId)
            .flatMapMany(CosmosDataBase::collections)
            .map { c -> Mappers.getMapper(CollectionMapper::class.java).toDto(c) }
            .collect(Collectors.toUnmodifiableList())
            .flatMap { containers -> ServerResponse.ok().bodyValue(containers) }
    }

    fun getById(serverRequest: ServerRequest): Mono<ServerResponse> {
        val dbId = serverRequest.pathVariable("dbId")
        val colId = serverRequest.pathVariable("colId")
        return dbInterface.queryContainer(dbId, colId)
            .map { c -> Mappers.getMapper(CollectionMapper::class.java).toDto(c)}
            .flatMap { dto -> ServerResponse.ok().bodyValue(dto) }
            .switchIfEmpty(ServerResponse.notFound().build())
    }
}
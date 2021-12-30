package dbfaker.cosmos.dto

import dbfaker.cosmos.model.Database
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Mappings

data class DatabaseDto(
    val id: String,
    val _rid: String,
    val _etag: String,
    val _ts: Long
) {
    val _self = "/dbs/$_rid/"
    val _colls = "colls/"
    val _users = "users/"

}

data class DatabasesDto(val databases: List<DatabaseDto>) {
    val _rid = ""
}

@Mapper
abstract class DatabaseMapper {
    @Mappings(
        Mapping(target = "id", source = "name"),
        Mapping(target = "_rid", source = "rid")
    )
    abstract fun toDto(d: Database?): DatabaseDto?
}
package dbfaker.cosmos.dto

import dbfaker.CosmosCollection
import dbfaker.PartitionKey
import dbfaker.ResourceId
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Mappings

data class CollectionDto(
    val id: String,
    val _rid: String,
    private val parentDbId: String,
    val _ts: Long,
    val partitionKey: PartitionKey,
    val _etag: String,
) {
    val _self = "dbs/$parentDbId/colls/$id"
    val _docs = "docs/"
    val _sprocs = "sprocs/"
    val _triggers = "triggers/"
    val _udfs = "udfs/"
    val _conflicts = "conflicts/"
}

@Mapper
abstract class CollectionMapper {
    @Mappings(
        Mapping(target = "id", source = "name"),
        Mapping(target = "_rid", source = "rid"),
        Mapping(target = "_etag", source = "etag"),
        Mapping(target = "_ts", source = "ts")
    )
    abstract fun toDto(d: CosmosCollection): CollectionDto
    fun text(r: ResourceId): String = r.text

}
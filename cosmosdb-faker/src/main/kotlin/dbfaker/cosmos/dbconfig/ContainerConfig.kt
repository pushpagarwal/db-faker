package dbfaker.cosmos.dbconfig

import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jsonMapper
import dbfaker.JsonDocument
import dbfaker.cosmos.model.DBCollection
import dbfaker.cosmos.model.DbObject
import dbfaker.cosmos.model.PartitionKey
import dbfaker.cosmos.model.ResourceId
import dbfaker.memdb.InMemoryContainer
import java.io.File
import java.lang.IllegalArgumentException

data class ContainerConfig(
    val name: String,
    val rid: Int,
    val partitionKey: PartitionKey?,
    val dataFile: String?,
) {
    fun read(parentDbId: String, parentRid:ResourceId): DBCollection {
        val ridCalc = parentRid.copy(documentCollection = rid)
        val container = DBCollection(InMemoryContainer(String::class.java),name, ridCalc, parentDbId, partitionKey)
        if(dataFile != null){
           val node =  jsonMapper().readTree(File(dataFile))
           if(!node.isArray){
               throw IllegalArgumentException("Invalid Data File:$dataFile")
           }
            node.iterator().forEach {
                if(!it.isObject){
                    val text = it.toString()
                    throw IllegalArgumentException("Object expected but found:$text")
                }
                container.upsert(DbObject(it as ObjectNode, ridCalc)).block()
            }
        }
        return container
    }
}
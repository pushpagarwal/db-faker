package dbfaker.cosmos.dbconfig

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dbfaker.cosmos.model.Database
import dbfaker.cosmos.model.ResourceId
import java.io.File
import java.util.stream.Collectors

data class DBConfig(
    val rid: Int,
    val name: String,
    val containers: List<ContainerConfig>
) {
    fun initDB() : Database {
        val ridCalc = ResourceId(database = rid)
        val c = containers.stream().map{ c -> c.read(name, ridCalc)}
            .collect(Collectors.toList())
        return Database(ridCalc.text, name, c)
    }

    companion object {
        fun read(configFile: String) =
            jacksonObjectMapper().readValue<DBConfig>(File(configFile))
                .initDB()
    }
}

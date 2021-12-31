package dbfaker.cosmos.config

import dbfaker.cosmos.dbconfig.DBConfig
import dbfaker.adaptor.memdb.Database
import dbfaker.adaptor.memdb.SimpleMemDb
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AppConfig {
    @Bean
    fun documentsProvider(@Value("\${dbconfig}") dbConfigPath: String): List<Database> =
        listOf(DBConfig.read(dbConfigPath))

    @Bean
    fun dbInterface(databases: List<Database>) = SimpleMemDb(databases)
}
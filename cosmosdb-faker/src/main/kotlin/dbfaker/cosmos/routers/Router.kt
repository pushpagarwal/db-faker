package dbfaker.cosmos.routers

import dbfaker.cosmos.handlers.CollectionHandler
import dbfaker.cosmos.handlers.DbDocumentHandler
import dbfaker.cosmos.handlers.DbHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.router

@Configuration
class Router {
    @Bean
    fun routeDbs(handler: DbHandler) = router {
        accept(MediaType.APPLICATION_JSON).nest {
            GET("/").invoke(handler::root)
            GET("/dbs").invoke(handler::listDatabases)
            GET("/dbs/{id}").invoke(handler::getDatabase)

        }
    }

    @Bean
    fun routeColls(handler: CollectionHandler) = router {
        accept(MediaType.APPLICATION_JSON).nest {
            GET("/dbs/{dbId}/colls").invoke(handler::list)
            GET("/dbs/{dbId}/colls/").invoke(handler::list)
            GET("/dbs/{dbId}/colls/{colId}").invoke(handler::getById)
        }
    }

    @Bean
    fun routeDocs(handler: DbDocumentHandler) = router {
        accept(MediaType.APPLICATION_JSON).nest {
            POST("/dbs/{dbId}/colls/{colId}/docs").invoke(handler::create)
            GET("/dbs/{dbId}/colls/{colId}/docs/").invoke(handler::create)
            GET("/dbs/{dbId}/colls/{colId}/docs/{docId}").invoke(handler::getById)
            PUT("/dbs/{dbId}/colls/{colId}/docs/{docId}").invoke(handler::updateById)
        }
    }
}
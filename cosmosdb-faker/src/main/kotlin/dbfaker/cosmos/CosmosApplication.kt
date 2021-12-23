package dbfaker.cosmos

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CosmosApplication

fun main(args: Array<String>) {
    runApplication<CosmosApplication>(*args)
}

package dbfaker.cosmos.error

import org.springframework.http.HttpStatus

@DslMarker
annotation class ErrorResponseDsl

data class ErrorResponse(
    val status: HttpStatus = HttpStatus.INTERNAL_SERVER_ERROR,
    val body: Map<String, Any> = emptyMap(),
    val headers: Map<String, String> = emptyMap(),
)

@ErrorResponseDsl
class ErrorResponseBuilder {
    private var status: HttpStatus = HttpStatus.INTERNAL_SERVER_ERROR
    private var body: Map<String, Any> = mapOf()
    private var headers: Map<String, String> = mapOf()

    fun status(builderAction: () -> HttpStatus) {
        status = builderAction()
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun body(
        builderAction: MutableMap<String, Any>.() -> Unit
    ) {
        body = buildMap(builderAction)
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun headers(
        builderAction: MutableMap<String, String>.() -> Unit
    ) {
        headers = buildMap(builderAction)
    }

    fun build() = ErrorResponse(status, body, headers)

}

fun errorResponse(builderAction: ErrorResponseBuilder.() -> Unit) = ErrorResponseBuilder().apply(builderAction).build()


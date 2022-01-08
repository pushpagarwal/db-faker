package dbfaker.cosmos.error

import dbfaker.memdb.exceptions.NotFound
import dbfaker.memdb.exceptions.PreConditionFailed
import dbfaker.parser.error.ParseCancellationException
import org.springframework.boot.autoconfigure.web.WebProperties
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes
import org.springframework.boot.web.reactive.error.ErrorAttributes
import org.springframework.context.ApplicationContext
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.*
import reactor.core.publisher.Mono

@Order(-3)
@Component
class CosmosWebExceptionHandler(
    attributes: DefaultErrorAttributes,
    context: ApplicationContext,
    serverCodecConfigurer: ServerCodecConfigurer
) :
    AbstractErrorWebExceptionHandler(attributes, WebProperties.Resources(), context) {

    init {
        super.setMessageReaders(serverCodecConfigurer.readers)
        super.setMessageWriters(serverCodecConfigurer.writers)
    }

    override fun getRoutingFunction(errorAttributes: ErrorAttributes): RouterFunction<ServerResponse> {
        return RouterFunctions.route(RequestPredicates.all(), this::renderResponse)
    }

    private fun renderResponse(request: ServerRequest): Mono<ServerResponse> {

        return Mono.deferContextual { Mono.just(it) }
            .flatMap {
                val response = when (val t = this.getError(request)) {
                    is IllegalArgumentException,
                    is ParseCancellationException -> handleBadRequest(t)
                    is NotFound -> handle(t)
                    is PreConditionFailed -> handle(t)
                    else -> handle(t)
                }
                ServerResponse.status(response.status)
                    .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                    .headers { headers -> response.headers.forEach { (k, v) -> headers.set(k, v) } }
                    .bodyValue(response.body);
            }
    }

    private fun handleBadRequest(ex: Throwable) =
        errorResponse {
            status { HttpStatus.BAD_REQUEST }
            body {
                ex.message?.let { put("message", it) }
            }
        }

    private fun handle(ex: Throwable) =
        errorResponse {
            body {
                ex.message?.let { put("message", it) }
            }
        }

    private fun handle(ex: NotFound) =
        errorResponse {
            status { HttpStatus.NOT_FOUND }
        }

    private fun handle(ex: PreConditionFailed) =
        errorResponse {
            status { HttpStatus.PRECONDITION_FAILED }
        }

}
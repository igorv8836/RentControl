package org.igorv8836.rentcontrol.server.foundation.errors

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.plugins.ContentTransformationException
import io.ktor.server.plugins.callid.callId
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import kotlinx.serialization.Serializable

@Serializable
data class ApiErrorDetail(
    val field: String? = null,
    val issue: String,
)

@Serializable
data class ApiErrorResponse(
    val code: String,
    val message: String,
    val details: List<ApiErrorDetail> = emptyList(),
    val traceId: String? = null,
)

class ApiException(
    val status: HttpStatusCode,
    val code: String,
    override val message: String,
    val details: List<ApiErrorDetail> = emptyList(),
) : RuntimeException(message)

fun Application.installErrorHandling() {
    install(StatusPages) {
        exception<ApiException> { call, cause ->
            call.respond(
                status = cause.status,
                message = ApiErrorResponse(
                    code = cause.code,
                    message = cause.message,
                    details = cause.details,
                    traceId = call.callId,
                ),
            )
        }

        exception<ContentTransformationException> { call, _ ->
            call.respond(
                status = HttpStatusCode.BadRequest,
                message = ApiErrorResponse(
                    code = "bad_request",
                    message = "Invalid request body",
                    traceId = call.callId,
                ),
            )
        }

        exception<Throwable> { call, cause ->
            call.application.environment.log.error("Unhandled error", cause)
            call.respond(
                status = HttpStatusCode.InternalServerError,
                message = ApiErrorResponse(
                    code = "internal_error",
                    message = "Internal server error",
                    traceId = call.callId,
                ),
            )
        }
    }
}

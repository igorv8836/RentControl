package org.igorv8836.rentcontrol.server.foundation.security

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.request.path
import org.igorv8836.rentcontrol.server.foundation.errors.ApiException

interface AccessTokenAuthenticator {
    suspend fun authenticate(accessToken: String): UserContext?
}

class BearerAuthConfig {
    lateinit var sessionsRepository: AccessTokenAuthenticator
}

val BearerAuth = createApplicationPlugin(name = "BearerAuth", createConfiguration = ::BearerAuthConfig) {
    val sessionsRepository = pluginConfig.sessionsRepository

    suspend fun authenticate(call: ApplicationCall) {
        val header = call.request.headers[HttpHeaders.Authorization]
            ?: throw ApiException(
                status = HttpStatusCode.Unauthorized,
                code = "unauthorized",
                message = "Missing Authorization header",
            )

        val prefix = "Bearer "
        if (!header.startsWith(prefix)) {
            throw ApiException(
                status = HttpStatusCode.Unauthorized,
                code = "unauthorized",
                message = "Invalid Authorization header",
            )
        }

        val token = header.removePrefix(prefix).trim()
        if (token.isBlank()) {
            throw ApiException(
                status = HttpStatusCode.Unauthorized,
                code = "unauthorized",
                message = "Invalid Authorization header",
            )
        }

        val user = sessionsRepository.authenticate(token)
            ?: throw ApiException(
                status = HttpStatusCode.Unauthorized,
                code = "unauthorized",
                message = "Invalid or expired token",
            )

        if (user.status == UserStatus.BLOCKED) {
            throw ApiException(
                status = HttpStatusCode.Forbidden,
                code = "account_blocked",
                message = "Account is blocked",
            )
        }

        call.attributes.put(UserContextKey, user)
    }

    onCall { call ->
        val path = call.request.path()
        if (!path.startsWith("/api/v1")) {
            return@onCall
        }
        if (path.startsWith("/api/v1/auth")) {
            return@onCall
        }
        authenticate(call)
    }
}

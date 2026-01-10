package org.igorv8836.rentcontrol.server.modules.auth.module

import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.igorv8836.rentcontrol.server.modules.auth.api.dto.ConfirmRequest
import org.igorv8836.rentcontrol.server.modules.auth.api.dto.LoginRequest
import org.igorv8836.rentcontrol.server.modules.auth.api.dto.LogoutRequest
import org.igorv8836.rentcontrol.server.modules.auth.api.dto.RefreshRequest
import org.igorv8836.rentcontrol.server.modules.auth.api.dto.RegisterRequest
import org.igorv8836.rentcontrol.server.modules.auth.api.dto.ResetPasswordConfirmRequest
import org.igorv8836.rentcontrol.server.modules.auth.api.dto.ResetPasswordRequest
import org.igorv8836.rentcontrol.server.modules.auth.api.dto.StatusResponse
import org.igorv8836.rentcontrol.server.modules.auth.api.dto.TokenPairResponse
import org.igorv8836.rentcontrol.server.modules.auth.domain.model.IssuedTokenPair
import org.igorv8836.rentcontrol.server.modules.auth.domain.service.AuthService
import java.time.Duration
import java.time.Instant

fun Route.authModule(authService: AuthService) {
    route("/auth") {
        post("/register") {
            val request = call.receive<RegisterRequest>()
            authService.register(request.email, request.password)
            call.respond(StatusResponse(status = "OTP_SENT"))
        }

        post("/confirm") {
            val request = call.receive<ConfirmRequest>()
            val tokens = authService.confirm(request.email, request.otp)
            call.respond(tokens.toResponse())
        }

        post("/login") {
            val request = call.receive<LoginRequest>()
            val tokens = authService.login(request.email, request.password)
            call.respond(tokens.toResponse())
        }

        post("/refresh") {
            val request = call.receive<RefreshRequest>()
            val tokens = authService.refresh(request.refreshToken)
            call.respond(tokens.toResponse())
        }

        post("/logout") {
            val request = call.receive<LogoutRequest>()
            authService.logout(request.refreshToken)
            call.respond(StatusResponse(status = "OK"))
        }

        post("/password/reset/request") {
            val request = call.receive<ResetPasswordRequest>()
            authService.requestPasswordReset(request.email)
            call.respond(StatusResponse(status = "OTP_SENT"))
        }

        post("/password/reset/confirm") {
            val request = call.receive<ResetPasswordConfirmRequest>()
            authService.confirmPasswordReset(request.email, request.otp, request.newPassword)
            call.respond(StatusResponse(status = "OK"))
        }
    }
}

private fun IssuedTokenPair.toResponse(): TokenPairResponse {
    val now = Instant.now()
    return TokenPairResponse(
        accessToken = access.token,
        refreshToken = refresh.token,
        accessExpiresInSeconds = Duration.between(now, access.expiresAt).seconds.coerceAtLeast(0),
        refreshExpiresInSeconds = Duration.between(now, refresh.expiresAt).seconds.coerceAtLeast(0),
    )
}


package org.igorv8836.rentcontrol.server.modules.auth.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
)

@Serializable
data class ConfirmRequest(
    val email: String,
    val otp: String,
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String,
)

@Serializable
data class RefreshRequest(
    val refreshToken: String,
)

@Serializable
data class LogoutRequest(
    val refreshToken: String,
)

@Serializable
data class ResetPasswordRequest(
    val email: String,
)

@Serializable
data class ResetPasswordConfirmRequest(
    val email: String,
    val otp: String,
    val newPassword: String,
)

@Serializable
data class StatusResponse(
    val status: String,
)

@Serializable
data class TokenPairResponse(
    val tokenType: String = "Bearer",
    val accessToken: String,
    val refreshToken: String,
    val accessExpiresInSeconds: Long,
    val refreshExpiresInSeconds: Long,
)


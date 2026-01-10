package org.igorv8836.rentcontrol.server.foundation.security

import io.ktor.server.application.ApplicationCall
import io.ktor.util.AttributeKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class UserRole {
    @SerialName("admin")
    ADMIN,

    @SerialName("landlord")
    LANDLORD,

    @SerialName("inspector")
    INSPECTOR,

    @SerialName("tenant")
    TENANT,
}

@Serializable
enum class UserStatus {
    @SerialName("pending")
    PENDING,

    @SerialName("active")
    ACTIVE,

    @SerialName("blocked")
    BLOCKED,
}

@Serializable
data class UserContext(
    val userId: Long,
    val email: String,
    val role: UserRole,
    val status: UserStatus,
)

internal val UserContextKey = AttributeKey<UserContext>("UserContext")

val ApplicationCall.userContext: UserContext
    get() = attributes[UserContextKey]


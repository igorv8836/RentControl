package org.igorv8836.rentcontrol.server.modules.users.domain.model

import kotlinx.serialization.json.JsonObject
import org.igorv8836.rentcontrol.server.foundation.security.UserRole
import org.igorv8836.rentcontrol.server.foundation.security.UserStatus
import java.time.Instant

data class User(
    val id: Long,
    val email: String,
    val fullName: String,
    val phone: String?,
    val role: UserRole,
    val status: UserStatus,
    val passwordHash: String,
    val preferences: JsonObject,
    val createdAt: Instant,
    val updatedAt: Instant,
)


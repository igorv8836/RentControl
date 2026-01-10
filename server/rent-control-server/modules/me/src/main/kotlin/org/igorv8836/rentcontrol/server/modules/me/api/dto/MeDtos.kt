package org.igorv8836.rentcontrol.server.modules.me.api.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import org.igorv8836.rentcontrol.server.foundation.security.UserRole
import org.igorv8836.rentcontrol.server.foundation.security.UserStatus

@Serializable
data class MeResponse(
    val id: Long,
    val email: String,
    val fullName: String,
    val phone: String? = null,
    val role: UserRole,
    val status: UserStatus,
    val preferences: JsonObject,
)

@Serializable
data class UpdateMeRequest(
    val fullName: String? = null,
    val phone: String? = null,
    val preferences: JsonObject? = null,
)

@Serializable
data class StatusResponse(
    val status: String,
)


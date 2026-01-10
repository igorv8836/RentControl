package org.igorv8836.rentcontrol.server.modules.me.domain.service

import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.JsonObject
import org.igorv8836.rentcontrol.server.foundation.errors.ApiException
import org.igorv8836.rentcontrol.server.modules.auth.domain.port.AuthSessionRepository
import org.igorv8836.rentcontrol.server.modules.users.domain.model.User
import org.igorv8836.rentcontrol.server.modules.users.domain.port.UsersRepository

class MeService(
    private val usersRepository: UsersRepository,
    private val sessionsRepository: AuthSessionRepository,
) {
    suspend fun getMe(userId: Long): User =
        usersRepository.getById(userId)
            ?: throw ApiException(
                status = HttpStatusCode.NotFound,
                code = "not_found",
                message = "User not found",
            )

    suspend fun updateMe(
        userId: Long,
        fullName: String?,
        phone: String?,
        preferences: JsonObject?,
    ): User = usersRepository.updateUser(
        userId = userId,
        fullName = fullName,
        phone = phone,
        preferences = preferences,
    )

    suspend fun logoutAll(userId: Long) {
        sessionsRepository.revokeAllForUser(userId)
    }
}


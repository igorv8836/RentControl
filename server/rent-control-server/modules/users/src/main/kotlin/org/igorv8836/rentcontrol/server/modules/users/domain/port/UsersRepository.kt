package org.igorv8836.rentcontrol.server.modules.users.domain.port

import kotlinx.serialization.json.JsonObject
import org.igorv8836.rentcontrol.server.foundation.security.UserRole
import org.igorv8836.rentcontrol.server.foundation.security.UserStatus
import org.igorv8836.rentcontrol.server.modules.users.domain.model.User

interface UsersRepository {
    suspend fun findByEmail(email: String): User?

    suspend fun getById(userId: Long): User?

    suspend fun createUser(
        email: String,
        passwordHash: String,
        role: UserRole,
        status: UserStatus,
    ): User

    suspend fun updateUser(
        userId: Long,
        fullName: String?,
        phone: String?,
        preferences: JsonObject?,
    ): User

    suspend fun updatePassword(
        userId: Long,
        passwordHash: String,
    )

    suspend fun updateStatus(
        userId: Long,
        status: UserStatus,
    )
}


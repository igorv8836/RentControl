package org.igorv8836.rentcontrol.server.modules.users.data.repo

import kotlinx.serialization.json.JsonObject
import org.igorv8836.rentcontrol.server.foundation.db.UsersTable
import org.igorv8836.rentcontrol.server.foundation.errors.ApiException
import org.igorv8836.rentcontrol.server.foundation.security.UserRole
import org.igorv8836.rentcontrol.server.foundation.security.UserStatus
import org.igorv8836.rentcontrol.server.foundation.security.toDbValue
import org.igorv8836.rentcontrol.server.foundation.security.userRoleFromDb
import org.igorv8836.rentcontrol.server.foundation.security.userStatusFromDb
import org.igorv8836.rentcontrol.server.modules.users.domain.model.User
import org.igorv8836.rentcontrol.server.modules.users.domain.port.UsersRepository
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.OffsetDateTime
import java.time.ZoneOffset

class ExposedUsersRepository(
    private val database: Database,
) : UsersRepository {
    override suspend fun findByEmail(email: String): User? = newSuspendedTransaction(db = database) {
        UsersTable
            .selectAll()
            .where { UsersTable.email eq email.lowercase() }
            .limit(1)
            .singleOrNull()
            ?.toUser()
    }

    override suspend fun getById(userId: Long): User? = newSuspendedTransaction(db = database) {
        UsersTable
            .selectAll()
            .where { UsersTable.id eq userId }
            .limit(1)
            .singleOrNull()
            ?.toUser()
    }

    override suspend fun createUser(
        email: String,
        passwordHash: String,
        role: UserRole,
        status: UserStatus,
    ): User = newSuspendedTransaction(db = database) {
        val now = OffsetDateTime.now(ZoneOffset.UTC)
        val id = UsersTable.insertAndGetId {
            it[UsersTable.email] = email.lowercase()
            it[UsersTable.passwordHash] = passwordHash
            it[UsersTable.role] = role.toDbValue()
            it[UsersTable.status] = status.toDbValue()
            it[UsersTable.createdAt] = now
            it[UsersTable.updatedAt] = now
        }.value
        getById(id) ?: error("Failed to load created user")
    }

    override suspend fun updateUser(
        userId: Long,
        fullName: String?,
        phone: String?,
        preferences: JsonObject?,
    ): User = newSuspendedTransaction(db = database) {
        val now = OffsetDateTime.now(ZoneOffset.UTC)
        val updated = UsersTable.update({ UsersTable.id eq userId }) { row ->
            fullName?.let { row[UsersTable.fullName] = it }
            if (phone != null) {
                row[UsersTable.phone] = phone
            }
            preferences?.let { row[UsersTable.preferences] = it }
            row[UsersTable.updatedAt] = now
        }
        if (updated == 0) {
            throw ApiException(
                status = io.ktor.http.HttpStatusCode.NotFound,
                code = "not_found",
                message = "User not found",
            )
        }
        getById(userId) ?: error("Failed to load updated user")
    }

    override suspend fun updatePassword(userId: Long, passwordHash: String) {
        newSuspendedTransaction(db = database) {
            val now = OffsetDateTime.now(ZoneOffset.UTC)
            UsersTable.update({ UsersTable.id eq userId }) { row ->
                row[UsersTable.passwordHash] = passwordHash
                row[UsersTable.updatedAt] = now
            }
        }
    }

    override suspend fun updateStatus(userId: Long, status: UserStatus) {
        newSuspendedTransaction(db = database) {
            val now = OffsetDateTime.now(ZoneOffset.UTC)
            UsersTable.update({ UsersTable.id eq userId }) { row ->
                row[UsersTable.status] = status.toDbValue()
                row[UsersTable.updatedAt] = now
            }
        }
    }

    private fun ResultRow.toUser(): User = User(
        id = this[UsersTable.id].value,
        email = this[UsersTable.email],
        fullName = this[UsersTable.fullName],
        phone = this[UsersTable.phone],
        role = userRoleFromDb(this[UsersTable.role]),
        status = userStatusFromDb(this[UsersTable.status]),
        passwordHash = this[UsersTable.passwordHash],
        preferences = this[UsersTable.preferences],
        createdAt = this[UsersTable.createdAt].toInstant(),
        updatedAt = this[UsersTable.updatedAt].toInstant(),
    )
}


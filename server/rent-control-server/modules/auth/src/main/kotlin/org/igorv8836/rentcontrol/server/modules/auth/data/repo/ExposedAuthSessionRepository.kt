package org.igorv8836.rentcontrol.server.modules.auth.data.repo

import org.igorv8836.rentcontrol.server.foundation.db.AuthSessionsTable
import org.igorv8836.rentcontrol.server.foundation.db.UsersTable
import org.igorv8836.rentcontrol.server.foundation.security.AccessTokenAuthenticator
import org.igorv8836.rentcontrol.server.foundation.security.UserContext
import org.igorv8836.rentcontrol.server.foundation.security.userRoleFromDb
import org.igorv8836.rentcontrol.server.foundation.security.userStatusFromDb
import org.igorv8836.rentcontrol.server.modules.auth.domain.model.IssuedTokenPair
import org.igorv8836.rentcontrol.server.modules.auth.domain.port.AuthSessionRepository
import org.igorv8836.rentcontrol.server.modules.auth.domain.port.SessionRecord
import org.igorv8836.rentcontrol.server.modules.auth.domain.service.TokenService
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greater
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

class ExposedAuthSessionRepository(
    private val database: Database,
    private val tokenService: TokenService = TokenService(),
) : AuthSessionRepository, AccessTokenAuthenticator {
    override suspend fun createSession(userId: Long, issuedTokens: IssuedTokenPair): SessionRecord =
        newSuspendedTransaction(db = database) {
            val now = OffsetDateTime.now(ZoneOffset.UTC)
            val id = AuthSessionsTable.insertAndGetId {
                it[AuthSessionsTable.userId] = EntityID(userId, UsersTable)
                it[AuthSessionsTable.refreshTokenHash] = issuedTokens.refresh.hash
                it[AuthSessionsTable.refreshExpiresAt] = issuedTokens.refresh.expiresAt.toOffsetDateTime()
                it[AuthSessionsTable.accessTokenHash] = issuedTokens.access.hash
                it[AuthSessionsTable.accessExpiresAt] = issuedTokens.access.expiresAt.toOffsetDateTime()
                it[AuthSessionsTable.createdAt] = now
                it[AuthSessionsTable.updatedAt] = now
            }.value

            SessionRecord(sessionId = id, userId = userId)
        }

    override suspend fun rotateSession(refreshTokenHash: String, newIssuedTokens: IssuedTokenPair): SessionRecord? =
        newSuspendedTransaction(db = database) {
            val nowInstant = Instant.now()
            val now = nowInstant.toOffsetDateTime()

            val session = AuthSessionsTable
                .selectAll()
                .where {
                    (AuthSessionsTable.refreshTokenHash eq refreshTokenHash) and
                        AuthSessionsTable.revokedAt.isNull() and
                        (AuthSessionsTable.refreshExpiresAt greater now)
                }
                .limit(1)
                .singleOrNull()
                ?: return@newSuspendedTransaction null

            val updated = AuthSessionsTable.update({ AuthSessionsTable.id eq session[AuthSessionsTable.id] }) { row ->
                row[AuthSessionsTable.refreshTokenHash] = newIssuedTokens.refresh.hash
                row[AuthSessionsTable.refreshExpiresAt] = newIssuedTokens.refresh.expiresAt.toOffsetDateTime()
                row[AuthSessionsTable.accessTokenHash] = newIssuedTokens.access.hash
                row[AuthSessionsTable.accessExpiresAt] = newIssuedTokens.access.expiresAt.toOffsetDateTime()
                row[AuthSessionsTable.updatedAt] = now
            }
            if (updated == 0) return@newSuspendedTransaction null

            SessionRecord(
                sessionId = session[AuthSessionsTable.id].value,
                userId = session[AuthSessionsTable.userId].value,
            )
        }

    override suspend fun revokeByRefreshTokenHash(refreshTokenHash: String): Boolean =
        newSuspendedTransaction(db = database) {
            val now = OffsetDateTime.now(ZoneOffset.UTC)
            AuthSessionsTable.update({
                (AuthSessionsTable.refreshTokenHash eq refreshTokenHash) and
                    AuthSessionsTable.revokedAt.isNull()
            }) { row ->
                row[AuthSessionsTable.revokedAt] = now
                row[AuthSessionsTable.updatedAt] = now
            } > 0
        }

    override suspend fun revokeAllForUser(userId: Long) {
        newSuspendedTransaction(db = database) {
            val now = OffsetDateTime.now(ZoneOffset.UTC)
            AuthSessionsTable.update({
                (AuthSessionsTable.userId eq EntityID(userId, UsersTable)) and
                    AuthSessionsTable.revokedAt.isNull()
            }) { row ->
                row[AuthSessionsTable.revokedAt] = now
                row[AuthSessionsTable.updatedAt] = now
            }
        }
    }

    override suspend fun findUserContextByAccessTokenHash(accessTokenHash: String): UserContext? =
        newSuspendedTransaction(db = database) {
            val now = OffsetDateTime.now(ZoneOffset.UTC)
            AuthSessionsTable
                .join(UsersTable, JoinType.INNER, additionalConstraint = { AuthSessionsTable.userId eq UsersTable.id })
                .selectAll()
                .where {
                    (AuthSessionsTable.accessTokenHash eq accessTokenHash) and
                        AuthSessionsTable.revokedAt.isNull() and
                        (AuthSessionsTable.accessExpiresAt greater now)
                }
                .limit(1)
                .singleOrNull()
                ?.toUserContext()
        }

    override suspend fun authenticate(accessToken: String): UserContext? {
        val hash = tokenService.hash(accessToken)
        return findUserContextByAccessTokenHash(hash)
    }

    private fun ResultRow.toUserContext(): UserContext = UserContext(
        userId = this[UsersTable.id].value,
        email = this[UsersTable.email],
        role = userRoleFromDb(this[UsersTable.role]),
        status = userStatusFromDb(this[UsersTable.status]),
    )

    private fun Instant.toOffsetDateTime(): OffsetDateTime = OffsetDateTime.ofInstant(this, ZoneOffset.UTC)
}

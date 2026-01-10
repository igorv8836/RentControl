package org.igorv8836.rentcontrol.server.modules.auth.domain.port

import org.igorv8836.rentcontrol.server.foundation.security.UserContext
import org.igorv8836.rentcontrol.server.modules.auth.domain.model.IssuedTokenPair

data class SessionRecord(
    val sessionId: Long,
    val userId: Long,
)

interface AuthSessionRepository {
    suspend fun createSession(userId: Long, issuedTokens: IssuedTokenPair): SessionRecord

    suspend fun rotateSession(refreshTokenHash: String, newIssuedTokens: IssuedTokenPair): SessionRecord?

    suspend fun revokeByRefreshTokenHash(refreshTokenHash: String): Boolean

    suspend fun revokeAllForUser(userId: Long)

    suspend fun findUserContextByAccessTokenHash(accessTokenHash: String): UserContext?
}


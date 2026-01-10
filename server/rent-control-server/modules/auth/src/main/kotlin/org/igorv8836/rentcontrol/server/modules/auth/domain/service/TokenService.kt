package org.igorv8836.rentcontrol.server.modules.auth.domain.service

import org.igorv8836.rentcontrol.server.modules.auth.domain.model.IssuedToken
import org.igorv8836.rentcontrol.server.modules.auth.domain.model.IssuedTokenPair
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.Duration
import java.time.Instant
import java.util.Base64

class TokenService(
    private val random: SecureRandom = SecureRandom(),
) {
    private val encoder = Base64.getUrlEncoder().withoutPadding()

    fun issueTokenPair(now: Instant = Instant.now()): IssuedTokenPair {
        val accessToken = newToken(ACCESS_TOKEN_BYTES)
        val refreshToken = newToken(REFRESH_TOKEN_BYTES)

        return IssuedTokenPair(
            access = IssuedToken(
                token = accessToken,
                hash = hash(accessToken),
                expiresAt = now.plus(ACCESS_TTL),
            ),
            refresh = IssuedToken(
                token = refreshToken,
                hash = hash(refreshToken),
                expiresAt = now.plus(REFRESH_TTL),
            ),
        )
    }

    fun hash(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(token.toByteArray(Charsets.UTF_8))
        return encoder.encodeToString(bytes)
    }

    private fun newToken(size: Int): String {
        val bytes = ByteArray(size).also(random::nextBytes)
        return encoder.encodeToString(bytes)
    }

    companion object {
        val ACCESS_TTL: Duration = Duration.ofMinutes(30)
        val REFRESH_TTL: Duration = Duration.ofDays(30)

        private const val ACCESS_TOKEN_BYTES = 32
        private const val REFRESH_TOKEN_BYTES = 48
    }
}


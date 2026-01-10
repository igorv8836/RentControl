package org.igorv8836.rentcontrol.server.modules.auth.domain.service

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class PasswordHasher(
    private val random: SecureRandom = SecureRandom(),
) {
    private val encoder = Base64.getUrlEncoder().withoutPadding()
    private val decoder = Base64.getUrlDecoder()

    fun hash(password: String): String {
        val salt = ByteArray(SALT_BYTES).also(random::nextBytes)
        val hash = pbkdf2(password, salt, DEFAULT_ITERATIONS)
        return "pbkdf2_sha256$${DEFAULT_ITERATIONS}$${encoder.encodeToString(salt)}$${encoder.encodeToString(hash)}"
    }

    fun verify(password: String, passwordHash: String): Boolean {
        val parts = passwordHash.split('$')
        if (parts.size != 4) return false
        if (parts[0] != "pbkdf2_sha256") return false
        val iterations = parts[1].toIntOrNull() ?: return false
        val salt = runCatching { decoder.decode(parts[2]) }.getOrNull() ?: return false
        val expected = runCatching { decoder.decode(parts[3]) }.getOrNull() ?: return false
        val actual = pbkdf2(password, salt, iterations)
        return MessageDigest.isEqual(expected, actual)
    }

    private fun pbkdf2(password: String, salt: ByteArray, iterations: Int): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, iterations, KEY_BITS)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        return factory.generateSecret(spec).encoded
    }

    private companion object {
        private const val DEFAULT_ITERATIONS = 120_000
        private const val SALT_BYTES = 16
        private const val KEY_BITS = 256
    }
}


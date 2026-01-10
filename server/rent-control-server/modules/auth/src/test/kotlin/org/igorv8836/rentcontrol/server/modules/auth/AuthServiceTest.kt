package org.igorv8836.rentcontrol.server.modules.auth

import kotlinx.serialization.json.JsonObject
import org.igorv8836.rentcontrol.server.foundation.errors.ApiException
import org.igorv8836.rentcontrol.server.foundation.security.UserContext
import org.igorv8836.rentcontrol.server.foundation.security.UserRole
import org.igorv8836.rentcontrol.server.foundation.security.UserStatus
import org.igorv8836.rentcontrol.server.modules.auth.domain.model.IssuedTokenPair
import org.igorv8836.rentcontrol.server.modules.auth.domain.model.OtpPurpose
import org.igorv8836.rentcontrol.server.modules.auth.domain.port.AuthSessionRepository
import org.igorv8836.rentcontrol.server.modules.auth.domain.port.OtpConsumeResult
import org.igorv8836.rentcontrol.server.modules.auth.domain.port.OtpRepository
import org.igorv8836.rentcontrol.server.modules.auth.domain.port.OtpSender
import org.igorv8836.rentcontrol.server.modules.auth.domain.port.SessionRecord
import org.igorv8836.rentcontrol.server.modules.auth.domain.service.AuthService
import org.igorv8836.rentcontrol.server.modules.auth.domain.service.PasswordHasher
import org.igorv8836.rentcontrol.server.modules.auth.domain.service.TokenService
import org.igorv8836.rentcontrol.server.modules.users.domain.model.User
import org.igorv8836.rentcontrol.server.modules.users.domain.port.UsersListQuery
import org.igorv8836.rentcontrol.server.modules.users.domain.port.UsersPage
import org.igorv8836.rentcontrol.server.modules.users.domain.port.UsersRepository
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AuthServiceTest {
    @Test
    fun `register + confirm activates user and returns tokens`() = runTest {
        val usersRepo = FakeUsersRepository()
        val sessionsRepo = FakeSessionsRepository(usersRepo)
        val otpRepo = FakeOtpRepository()
        val otpSender = CapturingOtpSender()

        val service = AuthService(
            usersRepository = usersRepo,
            sessionsRepository = sessionsRepo,
            otpRepository = otpRepo,
            passwordHasher = PasswordHasher(),
            tokenService = TokenService(),
            otpSender = otpSender,
        )

        service.register("test@example.com", "password123")

        val otp = otpSender.lastCode
        assertNotNull(otp)

        val tokens = service.confirm("test@example.com", otp)
        assertTrue(tokens.access.token.isNotBlank())
        assertTrue(tokens.refresh.token.isNotBlank())

        val user = usersRepo.findByEmail("test@example.com")
        assertNotNull(user)
        assertEquals(UserStatus.ACTIVE, user.status)
    }

    @Test
    fun `login with wrong password returns invalid_credentials`() = runTest {
        val usersRepo = FakeUsersRepository()
        val sessionsRepo = FakeSessionsRepository(usersRepo)
        val otpRepo = FakeOtpRepository()
        val otpSender = CapturingOtpSender()
        val passwordHasher = PasswordHasher()

        val user = usersRepo.createUser(
            email = "user@example.com",
            passwordHash = passwordHasher.hash("correct"),
            role = UserRole.TENANT,
            status = UserStatus.ACTIVE,
        )

        val service = AuthService(
            usersRepository = usersRepo,
            sessionsRepository = sessionsRepo,
            otpRepository = otpRepo,
            passwordHasher = passwordHasher,
            tokenService = TokenService(),
            otpSender = otpSender,
        )

        val error = try {
            service.login(user.email, "wrong")
            null
        } catch (e: ApiException) {
            e
        }

        assertNotNull(error)
        assertEquals("invalid_credentials", error.code)
    }

    private class CapturingOtpSender : OtpSender {
        var lastEmail: String? = null
        var lastCode: String? = null

        override fun send(email: String, code: String) {
            lastEmail = email
            lastCode = code
        }
    }

    private class FakeUsersRepository : UsersRepository {
        private val usersById = linkedMapOf<Long, User>()
        private var nextId: Long = 1

        override suspend fun findByEmail(email: String): User? =
            usersById.values.firstOrNull { it.email == email.lowercase() }

        override suspend fun getById(userId: Long): User? = usersById[userId]

        override suspend fun listUsers(query: UsersListQuery): UsersPage {
            val search = query.search
            val filtered = usersById.values
                .asSequence()
                .filter { query.role == null || it.role == query.role }
                .filter { query.status == null || it.status == query.status }
                .filter { search == null || it.email.contains(search, ignoreCase = true) || it.fullName.contains(search, ignoreCase = true) }
                .toList()

            return UsersPage(
                page = query.page,
                pageSize = query.pageSize,
                total = filtered.size.toLong(),
                items = filtered,
            )
        }

        override suspend fun createUser(
            email: String,
            passwordHash: String,
            role: UserRole,
            status: UserStatus,
        ): User {
            val id = nextId++
            val now = Instant.now()
            val user = User(
                id = id,
                email = email.lowercase(),
                fullName = "",
                phone = null,
                role = role,
                status = status,
                passwordHash = passwordHash,
                preferences = JsonObject(emptyMap()),
                createdAt = now,
                updatedAt = now,
            )
            usersById[id] = user
            return user
        }

        override suspend fun updateUser(
            userId: Long,
            fullName: String?,
            phone: String?,
            preferences: JsonObject?,
        ): User {
            val current = usersById[userId] ?: error("User not found")
            val updated = current.copy(
                fullName = fullName ?: current.fullName,
                phone = phone ?: current.phone,
                preferences = preferences ?: current.preferences,
                updatedAt = Instant.now(),
            )
            usersById[userId] = updated
            return updated
        }

        override suspend fun updatePassword(userId: Long, passwordHash: String) {
            val current = usersById[userId] ?: error("User not found")
            usersById[userId] = current.copy(passwordHash = passwordHash, updatedAt = Instant.now())
        }

        override suspend fun updateStatus(userId: Long, status: UserStatus) {
            val current = usersById[userId] ?: error("User not found")
            usersById[userId] = current.copy(status = status, updatedAt = Instant.now())
        }
    }

    private class FakeOtpRepository : OtpRepository {
        private val active = mutableListOf<Record>()

        override suspend fun createOtp(
            email: String,
            userId: Long?,
            purpose: OtpPurpose,
            codeHash: String,
            expiresAt: Instant,
        ) {
            active += Record(email.lowercase(), userId, purpose, codeHash, expiresAt, consumed = false)
        }

        override suspend fun consumeOtp(
            email: String,
            purpose: OtpPurpose,
            codeHash: String,
            now: Instant,
        ): OtpConsumeResult {
            val record = active.lastOrNull { it.email == email.lowercase() && it.purpose == purpose && !it.consumed }
                ?: return OtpConsumeResult.Invalid
            if (record.expiresAt <= now) return OtpConsumeResult.Expired
            if (record.codeHash != codeHash) return OtpConsumeResult.Invalid
            record.consumed = true
            return OtpConsumeResult.Success(userId = record.userId)
        }

        private data class Record(
            val email: String,
            val userId: Long?,
            val purpose: OtpPurpose,
            val codeHash: String,
            val expiresAt: Instant,
            var consumed: Boolean,
        )
    }

    private class FakeSessionsRepository(
        private val usersRepository: FakeUsersRepository,
    ) : AuthSessionRepository {
        private val sessions = mutableMapOf<String, Long>()
        private val tokenService = TokenService()

        override suspend fun createSession(userId: Long, issuedTokens: IssuedTokenPair): SessionRecord {
            sessions[issuedTokens.access.hash] = userId
            return SessionRecord(sessionId = 1, userId = userId)
        }

        override suspend fun rotateSession(refreshTokenHash: String, newIssuedTokens: IssuedTokenPair): SessionRecord? = null

        override suspend fun revokeByRefreshTokenHash(refreshTokenHash: String): Boolean = false

        override suspend fun revokeAllForUser(userId: Long) = Unit

        override suspend fun findUserContextByAccessTokenHash(accessTokenHash: String): UserContext? {
            val userId = sessions[accessTokenHash] ?: return null
            val user = usersRepository.getById(userId) ?: return null
            return UserContext(userId = user.id, email = user.email, role = user.role, status = user.status)
        }

        suspend fun authenticate(accessToken: String): UserContext? =
            findUserContextByAccessTokenHash(tokenService.hash(accessToken))
    }

    private fun runTest(block: suspend () -> Unit) {
        kotlinx.coroutines.runBlocking { block() }
    }
}

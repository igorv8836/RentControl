package org.igorv8836.rentcontrol.server.modules.me

import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.install
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.igorv8836.rentcontrol.server.foundation.errors.installErrorHandling
import org.igorv8836.rentcontrol.server.foundation.http.installHttpBasics
import org.igorv8836.rentcontrol.server.foundation.security.AccessTokenAuthenticator
import org.igorv8836.rentcontrol.server.foundation.security.BearerAuth
import org.igorv8836.rentcontrol.server.foundation.security.UserContext
import org.igorv8836.rentcontrol.server.foundation.security.UserRole
import org.igorv8836.rentcontrol.server.foundation.security.UserStatus
import org.igorv8836.rentcontrol.server.modules.auth.domain.model.IssuedTokenPair
import org.igorv8836.rentcontrol.server.modules.auth.domain.port.AuthSessionRepository
import org.igorv8836.rentcontrol.server.modules.auth.domain.port.SessionRecord
import org.igorv8836.rentcontrol.server.modules.me.domain.service.MeService
import org.igorv8836.rentcontrol.server.modules.me.module.meModule
import org.igorv8836.rentcontrol.server.modules.users.domain.model.User
import org.igorv8836.rentcontrol.server.modules.users.domain.port.UsersRepository
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

class MeModuleTest {
    @Test
    fun `GET me requires bearer token`() = testApplication {
        val usersRepo = FakeUsersRepository()
        val sessionsRepo = FakeSessionsRepository(
            tokenToUser = mapOf("token" to UserContext(1, "user@example.com", UserRole.TENANT, UserStatus.ACTIVE)),
        )

        usersRepo.seed(
            User(
                id = 1,
                email = "user@example.com",
                fullName = "User",
                phone = null,
                role = UserRole.TENANT,
                status = UserStatus.ACTIVE,
                passwordHash = "hash",
                preferences = Json.parseToJsonElement("{}").jsonObject,
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
            ),
        )

        application {
            installHttpBasics()
            installErrorHandling()
            install(BearerAuth) { sessionsRepository = sessionsRepo }
            routing {
                route("/api/v1") {
                    meModule(MeService(usersRepo, sessionsRepo))
                }
            }
        }

        val unauthorized = client.get("/api/v1/me")
        assertEquals(HttpStatusCode.Unauthorized, unauthorized.status)

        val ok = client.get("/api/v1/me") {
            header(HttpHeaders.Authorization, "Bearer token")
        }
        assertEquals(HttpStatusCode.OK, ok.status)

        val json = Json.parseToJsonElement(ok.bodyAsText()).jsonObject
        assertEquals("user@example.com", json["email"]?.jsonPrimitive?.content)
    }

    private class FakeUsersRepository : UsersRepository {
        private val usersById = mutableMapOf<Long, User>()

        fun seed(user: User) {
            usersById[user.id] = user
        }

        override suspend fun findByEmail(email: String): User? = usersById.values.firstOrNull { it.email == email }

        override suspend fun getById(userId: Long): User? = usersById[userId]

        override suspend fun createUser(email: String, passwordHash: String, role: UserRole, status: UserStatus): User {
            error("Not needed in test")
        }

        override suspend fun updateUser(userId: Long, fullName: String?, phone: String?, preferences: kotlinx.serialization.json.JsonObject?): User {
            error("Not needed in test")
        }

        override suspend fun updatePassword(userId: Long, passwordHash: String) = Unit

        override suspend fun updateStatus(userId: Long, status: UserStatus) = Unit
    }

    private class FakeSessionsRepository(
        private val tokenToUser: Map<String, UserContext>,
    ) : AuthSessionRepository, AccessTokenAuthenticator {
        override suspend fun authenticate(accessToken: String): UserContext? = tokenToUser[accessToken]

        override suspend fun createSession(userId: Long, issuedTokens: IssuedTokenPair): SessionRecord {
            error("Not needed in test")
        }

        override suspend fun rotateSession(refreshTokenHash: String, newIssuedTokens: IssuedTokenPair): SessionRecord? {
            error("Not needed in test")
        }

        override suspend fun revokeByRefreshTokenHash(refreshTokenHash: String): Boolean = false

        override suspend fun revokeAllForUser(userId: Long) = Unit

        override suspend fun findUserContextByAccessTokenHash(accessTokenHash: String): UserContext? {
            error("Not needed in test")
        }
    }
}

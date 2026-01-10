package org.igorv8836.rentcontrol.server.modules.tenants

import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.application.install
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import org.igorv8836.rentcontrol.server.foundation.errors.installErrorHandling
import org.igorv8836.rentcontrol.server.foundation.http.installHttpBasics
import org.igorv8836.rentcontrol.server.foundation.security.AccessTokenAuthenticator
import org.igorv8836.rentcontrol.server.foundation.security.BearerAuth
import org.igorv8836.rentcontrol.server.foundation.security.UserContext
import org.igorv8836.rentcontrol.server.foundation.security.UserRole
import org.igorv8836.rentcontrol.server.foundation.security.UserStatus
import org.igorv8836.rentcontrol.server.modules.tenants.domain.service.TenantsService
import org.igorv8836.rentcontrol.server.modules.tenants.module.tenantsModule
import org.igorv8836.rentcontrol.server.modules.users.domain.model.User
import org.igorv8836.rentcontrol.server.modules.users.domain.port.UsersListQuery
import org.igorv8836.rentcontrol.server.modules.users.domain.port.UsersPage
import org.igorv8836.rentcontrol.server.modules.users.domain.port.UsersRepository
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TenantsModuleTest {
    @Test
    fun `GET tenants requires bearer token`() = testApplication {
        val usersRepo = FakeUsersRepository()
        val sessionsRepo = FakeSessionsRepository(
            tokenToUser = mapOf(
                "landlordToken" to UserContext(1, "landlord@example.com", UserRole.LANDLORD, UserStatus.ACTIVE),
            ),
        )

        application {
            installHttpBasics()
            installErrorHandling()
            install(BearerAuth) { sessionsRepository = sessionsRepo }
            routing {
                route("/api/v1") {
                    tenantsModule(TenantsService(usersRepo))
                }
            }
        }

        val unauthorized = client.get("/api/v1/tenants")
        assertEquals(HttpStatusCode.Unauthorized, unauthorized.status)

        val ok = client.get("/api/v1/tenants") {
            header(HttpHeaders.Authorization, "Bearer landlordToken")
        }
        assertEquals(HttpStatusCode.OK, ok.status)
    }

    @Test
    fun `tenant cannot access tenants list`() = testApplication {
        val usersRepo = FakeUsersRepository()
        val sessionsRepo = FakeSessionsRepository(
            tokenToUser = mapOf(
                "tenantToken" to UserContext(2, "tenant@example.com", UserRole.TENANT, UserStatus.ACTIVE),
            ),
        )

        application {
            installHttpBasics()
            installErrorHandling()
            install(BearerAuth) { sessionsRepository = sessionsRepo }
            routing {
                route("/api/v1") {
                    tenantsModule(TenantsService(usersRepo))
                }
            }
        }

        val response = client.get("/api/v1/tenants") {
            header(HttpHeaders.Authorization, "Bearer tenantToken")
        }
        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `landlord can create tenant and list`() = testApplication {
        val usersRepo = FakeUsersRepository()
        val sessionsRepo = FakeSessionsRepository(
            tokenToUser = mapOf(
                "landlordToken" to UserContext(1, "landlord@example.com", UserRole.LANDLORD, UserStatus.ACTIVE),
            ),
        )

        application {
            installHttpBasics()
            installErrorHandling()
            install(BearerAuth) { sessionsRepository = sessionsRepo }
            routing {
                route("/api/v1") {
                    tenantsModule(TenantsService(usersRepo))
                }
            }
        }

        val created = client.post("/api/v1/tenants") {
            header(HttpHeaders.Authorization, "Bearer landlordToken")
            contentType(ContentType.Application.Json)
            setBody("""{"email":"tenant@example.com","fullName":"Tenant A","phone":"+123"}""")
        }
        assertEquals(HttpStatusCode.Created, created.status)

        val createdJson = Json.parseToJsonElement(created.bodyAsText()).jsonObject
        val tenantId = createdJson["id"]?.jsonPrimitive?.long ?: error("Missing id")
        assertEquals("tenant@example.com", createdJson["email"]?.jsonPrimitive?.content)
        assertEquals("Tenant A", createdJson["fullName"]?.jsonPrimitive?.content)
        assertEquals("active", createdJson["status"]?.jsonPrimitive?.content)

        val list = client.get("/api/v1/tenants") {
            header(HttpHeaders.Authorization, "Bearer landlordToken")
        }
        assertEquals(HttpStatusCode.OK, list.status)

        val listJson = Json.parseToJsonElement(list.bodyAsText()).jsonObject
        val items = listJson["items"]?.jsonArray ?: error("Missing items")
        assertEquals(1, items.size)
        assertEquals(tenantId, items[0].jsonObject["id"]?.jsonPrimitive?.long)
        assertEquals("tenant@example.com", items[0].jsonObject["email"]?.jsonPrimitive?.content)

        val details = client.get("/api/v1/tenants/$tenantId") {
            header(HttpHeaders.Authorization, "Bearer landlordToken")
        }
        assertEquals(HttpStatusCode.OK, details.status)

        val detailsJson = Json.parseToJsonElement(details.bodyAsText()).jsonObject
        assertEquals("Tenant A", detailsJson["fullName"]?.jsonPrimitive?.content)
        assertTrue(detailsJson["phone"]?.jsonPrimitive?.content == "+123")
        assertTrue(detailsJson["isArchived"]?.jsonPrimitive?.booleanOrNull == null)
    }

    private class FakeSessionsRepository(
        private val tokenToUser: Map<String, UserContext>,
    ) : AccessTokenAuthenticator {
        override suspend fun authenticate(accessToken: String): UserContext? = tokenToUser[accessToken]
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
                preferences = Json.parseToJsonElement("{}").jsonObject,
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
            preferences: kotlinx.serialization.json.JsonObject?,
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

        override suspend fun updatePassword(userId: Long, passwordHash: String) = Unit

        override suspend fun updateStatus(userId: Long, status: UserStatus) = Unit
    }
}

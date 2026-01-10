package org.igorv8836.rentcontrol.server.modules.objects

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
import org.igorv8836.rentcontrol.server.modules.objects.domain.model.ObjectAggregates
import org.igorv8836.rentcontrol.server.modules.objects.domain.model.ObjectOccupancyStatus
import org.igorv8836.rentcontrol.server.modules.objects.domain.model.RentObject
import org.igorv8836.rentcontrol.server.modules.objects.domain.port.CreateObjectData
import org.igorv8836.rentcontrol.server.modules.objects.domain.port.ObjectsListQuery
import org.igorv8836.rentcontrol.server.modules.objects.domain.port.ObjectsPage
import org.igorv8836.rentcontrol.server.modules.objects.domain.port.ObjectsRepository
import org.igorv8836.rentcontrol.server.modules.objects.domain.port.UpdateObjectPatch
import org.igorv8836.rentcontrol.server.modules.objects.domain.service.ObjectsService
import org.igorv8836.rentcontrol.server.modules.objects.module.objectsModule
import org.igorv8836.rentcontrol.server.modules.users.domain.model.User
import org.igorv8836.rentcontrol.server.modules.users.domain.port.UsersRepository
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ObjectsModuleTest {
    @Test
    fun `GET objects requires bearer token`() = testApplication {
        val usersRepo = FakeUsersRepository()
        val objectsRepo = FakeObjectsRepository()
        val sessionsRepo = FakeSessionsRepository(
            tokenToUser = mapOf(
                "token" to UserContext(1, "landlord@example.com", UserRole.LANDLORD, UserStatus.ACTIVE),
            ),
        )

        application {
            installHttpBasics()
            installErrorHandling()
            install(BearerAuth) { sessionsRepository = sessionsRepo }
            routing {
                route("/api/v1") {
                    objectsModule(ObjectsService(objectsRepo, usersRepo))
                }
            }
        }

        val unauthorized = client.get("/api/v1/objects")
        assertEquals(HttpStatusCode.Unauthorized, unauthorized.status)

        val ok = client.get("/api/v1/objects") {
            header(HttpHeaders.Authorization, "Bearer token")
        }
        assertEquals(HttpStatusCode.OK, ok.status)
    }

    @Test
    fun `tenant cannot create object`() = testApplication {
        val usersRepo = FakeUsersRepository().apply {
            seed(
                User(
                    id = 2,
                    email = "tenant@example.com",
                    fullName = "Tenant",
                    phone = null,
                    role = UserRole.TENANT,
                    status = UserStatus.ACTIVE,
                    passwordHash = "hash",
                    preferences = Json.parseToJsonElement("{}").jsonObject,
                    createdAt = Instant.now(),
                    updatedAt = Instant.now(),
                ),
            )
        }
        val objectsRepo = FakeObjectsRepository()
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
                    objectsModule(ObjectsService(objectsRepo, usersRepo))
                }
            }
        }

        val response = client.post("/api/v1/objects") {
            header(HttpHeaders.Authorization, "Bearer tenantToken")
            contentType(ContentType.Application.Json)
            setBody("""{"address":"Test","type":"flat"}""")
        }
        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `landlord can create and list own objects`() = testApplication {
        val usersRepo = FakeUsersRepository().apply {
            seed(
                User(
                    id = 10,
                    email = "tenant@example.com",
                    fullName = "Tenant",
                    phone = null,
                    role = UserRole.TENANT,
                    status = UserStatus.ACTIVE,
                    passwordHash = "hash",
                    preferences = Json.parseToJsonElement("{}").jsonObject,
                    createdAt = Instant.now(),
                    updatedAt = Instant.now(),
                ),
            )
        }
        val objectsRepo = FakeObjectsRepository()
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
                    objectsModule(ObjectsService(objectsRepo, usersRepo))
                }
            }
        }

        val created = client.post("/api/v1/objects") {
            header(HttpHeaders.Authorization, "Bearer landlordToken")
            contentType(ContentType.Application.Json)
            setBody("""{"address":"Main street 1","type":"flat","tenantId":10}""")
        }
        assertEquals(HttpStatusCode.Created, created.status)

        val createdJson = Json.parseToJsonElement(created.bodyAsText()).jsonObject
        val objectId = createdJson["id"]?.jsonPrimitive?.long ?: error("Missing id")
        assertEquals("leased", createdJson["status"]?.jsonPrimitive?.content)
        assertTrue(createdJson["isArchived"]?.jsonPrimitive?.booleanOrNull == false)

        val list = client.get("/api/v1/objects") {
            header(HttpHeaders.Authorization, "Bearer landlordToken")
        }
        assertEquals(HttpStatusCode.OK, list.status)

        val listJson = Json.parseToJsonElement(list.bodyAsText()).jsonObject
        val items = listJson["items"]?.jsonArray ?: error("Missing items")
        assertEquals(1, items.size)
        assertEquals("Main street 1", items[0].jsonObject["address"]?.jsonPrimitive?.content)

        val details = client.get("/api/v1/objects/$objectId") {
            header(HttpHeaders.Authorization, "Bearer landlordToken")
        }
        assertEquals(HttpStatusCode.OK, details.status)

        val detailsJson = Json.parseToJsonElement(details.bodyAsText()).jsonObject
        val overview = detailsJson["overview"]?.jsonObject ?: error("Missing overview")
        assertEquals("2", overview["defects"]?.jsonObject?.get("openCount")?.jsonPrimitive?.content)
        assertEquals("1", overview["defects"]?.jsonObject?.get("overdueCount")?.jsonPrimitive?.content)
        assertEquals(
            "2026-01-10T00:00:00Z",
            overview["inspections"]?.jsonObject?.get("nextScheduledAt")?.jsonPrimitive?.content,
        )
    }

    private class FakeSessionsRepository(
        private val tokenToUser: Map<String, UserContext>,
    ) : AccessTokenAuthenticator {
        override suspend fun authenticate(accessToken: String): UserContext? = tokenToUser[accessToken]
    }

    private class FakeUsersRepository : UsersRepository {
        private val usersById = mutableMapOf<Long, User>()

        fun seed(user: User) {
            usersById[user.id] = user
        }

        override suspend fun findByEmail(email: String): User? =
            usersById.values.firstOrNull { it.email == email.lowercase() }

        override suspend fun getById(userId: Long): User? = usersById[userId]

        override suspend fun createUser(email: String, passwordHash: String, role: UserRole, status: UserStatus): User {
            error("Not needed in test")
        }

        override suspend fun updateUser(
            userId: Long,
            fullName: String?,
            phone: String?,
            preferences: kotlinx.serialization.json.JsonObject?,
        ): User {
            error("Not needed in test")
        }

        override suspend fun updatePassword(userId: Long, passwordHash: String) = Unit

        override suspend fun updateStatus(userId: Long, status: UserStatus) = Unit
    }

    private class FakeObjectsRepository : ObjectsRepository {
        private val objects = linkedMapOf<Long, RentObject>()
        private var nextId: Long = 1

        override suspend fun listForUser(user: UserContext, query: ObjectsListQuery): ObjectsPage {
            val filtered = objects.values
                .asSequence()
                .filter { query.includeArchived || !it.isArchived }
                .filter { query.status == null || it.status == query.status }
                .filter { query.search == null || it.address.contains(query.search, ignoreCase = true) }
                .filter { obj ->
                    when (user.role) {
                        UserRole.ADMIN, UserRole.INSPECTOR -> true
                        UserRole.LANDLORD -> obj.ownerId == user.userId
                        UserRole.TENANT -> obj.tenantId == user.userId
                    }
                }
                .toList()

            return ObjectsPage(
                page = query.page,
                pageSize = query.pageSize,
                total = filtered.size.toLong(),
                items = filtered,
            )
        }

        override suspend fun getForUser(user: UserContext, objectId: Long): RentObject? {
            val obj = objects[objectId] ?: return null
            val accessible = when (user.role) {
                UserRole.ADMIN, UserRole.INSPECTOR -> true
                UserRole.LANDLORD -> obj.ownerId == user.userId
                UserRole.TENANT -> obj.tenantId == user.userId
            }
            return if (accessible) obj else null
        }

        override suspend fun getAggregates(objectId: Long, now: Instant): ObjectAggregates =
            ObjectAggregates(
                defectsOpenCount = 2,
                defectsOverdueCount = 1,
                nextInspectionAt = Instant.parse("2026-01-10T00:00:00Z"),
                lastInspectionAt = Instant.parse("2026-01-09T00:00:00Z"),
                lastMeterReadingAt = Instant.parse("2026-01-08T00:00:00Z"),
            )

        override suspend fun create(data: CreateObjectData): RentObject {
            val id = nextId++
            val now = Instant.now()
            val obj = RentObject(
                id = id,
                address = data.address,
                type = data.type,
                area = data.area,
                status = data.status,
                notes = data.notes,
                ownerId = data.ownerId,
                tenantId = data.tenantId,
                archivedAt = null,
                createdAt = now,
                updatedAt = now,
            )
            objects[id] = obj
            return obj
        }

        override suspend fun updateForUser(user: UserContext, objectId: Long, patch: UpdateObjectPatch): RentObject? {
            val current = getForUser(user, objectId) ?: return null
            val updated = current.copy(
                address = patch.address ?: current.address,
                type = patch.type ?: current.type,
                area = patch.area ?: current.area,
                notes = patch.notes ?: current.notes,
                status = patch.status ?: current.status,
                tenantId = patch.tenantId ?: current.tenantId,
                updatedAt = Instant.now(),
            )
            objects[objectId] = updated
            return updated
        }

        override suspend fun setArchivedForUser(user: UserContext, objectId: Long, archived: Boolean): RentObject? {
            val current = getForUser(user, objectId) ?: return null
            val updated = current.copy(
                archivedAt = if (archived) Instant.now() else null,
                updatedAt = Instant.now(),
            )
            objects[objectId] = updated
            return updated
        }
    }
}

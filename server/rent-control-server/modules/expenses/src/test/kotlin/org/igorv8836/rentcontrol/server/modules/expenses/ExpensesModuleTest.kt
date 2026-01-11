package org.igorv8836.rentcontrol.server.modules.expenses

import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
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
import org.igorv8836.rentcontrol.server.modules.expenses.domain.model.Expense
import org.igorv8836.rentcontrol.server.modules.expenses.domain.model.ExpenseApproval
import org.igorv8836.rentcontrol.server.modules.expenses.domain.model.ExpenseApprovalStatus
import org.igorv8836.rentcontrol.server.modules.expenses.domain.model.ExpenseAttachment
import org.igorv8836.rentcontrol.server.modules.expenses.domain.model.ExpenseDetails
import org.igorv8836.rentcontrol.server.modules.expenses.domain.model.ExpenseStatus
import org.igorv8836.rentcontrol.server.modules.expenses.domain.model.ExpenseType
import org.igorv8836.rentcontrol.server.modules.expenses.domain.model.ExpenseUserSummary
import org.igorv8836.rentcontrol.server.modules.expenses.domain.port.CreateExpenseData
import org.igorv8836.rentcontrol.server.modules.expenses.domain.port.ExpensesListQuery
import org.igorv8836.rentcontrol.server.modules.expenses.domain.port.ExpensesPage
import org.igorv8836.rentcontrol.server.modules.expenses.domain.port.ExpensesRepository
import org.igorv8836.rentcontrol.server.modules.expenses.domain.port.UpdateExpensePatch
import org.igorv8836.rentcontrol.server.modules.expenses.domain.service.ExpensesService
import org.igorv8836.rentcontrol.server.modules.expenses.module.expensesModule
import org.igorv8836.rentcontrol.server.modules.users.domain.model.User
import org.igorv8836.rentcontrol.server.modules.users.domain.port.UsersListQuery
import org.igorv8836.rentcontrol.server.modules.users.domain.port.UsersPage
import org.igorv8836.rentcontrol.server.modules.users.domain.port.UsersRepository
import java.time.Instant
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ExpensesModuleTest {
    @Test
    fun `GET expenses requires bearer token`() = testApplication {
        val usersRepo = FakeUsersRepository()
        val expensesRepo = FakeExpensesRepository(usersRepo)
        val sessionsRepo = FakeSessionsRepository(
            tokenToUser = mapOf(
                "token" to UserContext(1, "user@example.com", UserRole.TENANT, UserStatus.ACTIVE),
            ),
        )

        application {
            installHttpBasics()
            installErrorHandling()
            install(BearerAuth) { sessionsRepository = sessionsRepo }
            routing {
                route("/api/v1") {
                    expensesModule(ExpensesService(expensesRepo, usersRepo))
                }
            }
        }

        val unauthorized = client.get("/api/v1/expenses")
        assertEquals(HttpStatusCode.Unauthorized, unauthorized.status)

        val ok = client.get("/api/v1/expenses") {
            header(HttpHeaders.Authorization, "Bearer token")
        }
        assertEquals(HttpStatusCode.OK, ok.status)
    }

    @Test
    fun `visibility rule - author and approver`() = testApplication {
        val usersRepo = FakeUsersRepository().apply {
            seedUser(1, "author@example.com")
            seedUser(2, "approver@example.com")
            seedUser(3, "other@example.com")
        }
        val expensesRepo = FakeExpensesRepository(usersRepo).apply {
            seedExpense(
                authorId = 1,
                approverIds = listOf(2),
            )
            seedExpense(
                authorId = 3,
                approverIds = listOf(1),
            )
            seedExpense(
                authorId = 3,
                approverIds = listOf(2),
            )
        }
        val sessionsRepo = FakeSessionsRepository(
            tokenToUser = mapOf(
                "authorToken" to UserContext(1, "author@example.com", UserRole.TENANT, UserStatus.ACTIVE),
                "approverToken" to UserContext(2, "approver@example.com", UserRole.TENANT, UserStatus.ACTIVE),
            ),
        )

        application {
            installHttpBasics()
            installErrorHandling()
            install(BearerAuth) { sessionsRepository = sessionsRepo }
            routing {
                route("/api/v1") {
                    expensesModule(ExpensesService(expensesRepo, usersRepo))
                }
            }
        }

        val authorList = client.get("/api/v1/expenses") {
            header(HttpHeaders.Authorization, "Bearer authorToken")
        }
        assertEquals(HttpStatusCode.OK, authorList.status)
        val authorItems = Json.parseToJsonElement(authorList.bodyAsText()).jsonObject["items"]?.jsonArray ?: error("Missing items")
        assertEquals(2, authorItems.size)

        val approverList = client.get("/api/v1/expenses") {
            header(HttpHeaders.Authorization, "Bearer approverToken")
        }
        assertEquals(HttpStatusCode.OK, approverList.status)
        val approverItems = Json.parseToJsonElement(approverList.bodyAsText()).jsonObject["items"]?.jsonArray ?: error("Missing items")
        assertEquals(2, approverItems.size)
    }

    @Test
    fun `create - submit - approve flow`() = testApplication {
        val usersRepo = FakeUsersRepository().apply {
            seedUser(1, "author@example.com")
            seedUser(2, "approver@example.com")
        }
        val expensesRepo = FakeExpensesRepository(usersRepo)
        val sessionsRepo = FakeSessionsRepository(
            tokenToUser = mapOf(
                "authorToken" to UserContext(1, "author@example.com", UserRole.TENANT, UserStatus.ACTIVE),
                "approverToken" to UserContext(2, "approver@example.com", UserRole.TENANT, UserStatus.ACTIVE),
            ),
        )

        application {
            installHttpBasics()
            installErrorHandling()
            install(BearerAuth) { sessionsRepository = sessionsRepo }
            routing {
                route("/api/v1") {
                    expensesModule(ExpensesService(expensesRepo, usersRepo))
                }
            }
        }

        val created = client.post("/api/v1/expenses") {
            header(HttpHeaders.Authorization, "Bearer authorToken")
            contentType(ContentType.Application.Json)
            setBody(
                """
                {
                  "objectId": 1,
                  "category": "Utilities",
                  "type": "fact",
                  "amount": 100.5,
                  "expenseDate": "2026-01-10",
                  "description": "Water bill",
                  "approverIds": [2]
                }
                """.trimIndent(),
            )
        }
        assertEquals(HttpStatusCode.Created, created.status)

        val createdJson = Json.parseToJsonElement(created.bodyAsText()).jsonObject
        val expenseId = createdJson["id"]?.jsonPrimitive?.long ?: error("Missing id")
        assertEquals("draft", createdJson["status"]?.jsonPrimitive?.content)

        val submit = client.post("/api/v1/expenses/$expenseId/submit") {
            header(HttpHeaders.Authorization, "Bearer authorToken")
        }
        assertEquals(HttpStatusCode.OK, submit.status)

        val submitJson = Json.parseToJsonElement(submit.bodyAsText()).jsonObject
        assertEquals("pending", submitJson["status"]?.jsonPrimitive?.content)
        assertNotNull(submitJson["submittedAt"]?.jsonPrimitive?.content)

        val approve = client.post("/api/v1/expenses/$expenseId/approve") {
            header(HttpHeaders.Authorization, "Bearer approverToken")
        }
        assertEquals(HttpStatusCode.OK, approve.status)

        val approveJson = Json.parseToJsonElement(approve.bodyAsText()).jsonObject
        assertEquals("approved", approveJson["status"]?.jsonPrimitive?.content)
    }

    @Test
    fun `author can edit draft expense`() = testApplication {
        val usersRepo = FakeUsersRepository().apply {
            seedUser(1, "author@example.com")
        }
        val expensesRepo = FakeExpensesRepository(usersRepo)
        val sessionsRepo = FakeSessionsRepository(
            tokenToUser = mapOf(
                "authorToken" to UserContext(1, "author@example.com", UserRole.TENANT, UserStatus.ACTIVE),
            ),
        )

        application {
            installHttpBasics()
            installErrorHandling()
            install(BearerAuth) { sessionsRepository = sessionsRepo }
            routing {
                route("/api/v1") {
                    expensesModule(ExpensesService(expensesRepo, usersRepo))
                }
            }
        }

        val created = client.post("/api/v1/expenses") {
            header(HttpHeaders.Authorization, "Bearer authorToken")
            contentType(ContentType.Application.Json)
            setBody(
                """
                {
                  "objectId": 1,
                  "category": "Utilities",
                  "type": "plan",
                  "amount": 10.0,
                  "expenseDate": "2026-01-10",
                  "description": "Planned",
                  "approverIds": []
                }
                """.trimIndent(),
            )
        }
        val expenseId = Json.parseToJsonElement(created.bodyAsText()).jsonObject["id"]?.jsonPrimitive?.long ?: error("Missing id")

        val updated = client.patch("/api/v1/expenses/$expenseId") {
            header(HttpHeaders.Authorization, "Bearer authorToken")
            contentType(ContentType.Application.Json)
            setBody("""{"amount": 20.0, "description": "Updated"}""")
        }
        assertEquals(HttpStatusCode.OK, updated.status)

        val updatedJson = Json.parseToJsonElement(updated.bodyAsText()).jsonObject
        assertEquals("Updated", updatedJson["description"]?.jsonPrimitive?.content)
    }

    private class FakeSessionsRepository(
        private val tokenToUser: Map<String, UserContext>,
    ) : AccessTokenAuthenticator {
        override suspend fun authenticate(accessToken: String): UserContext? = tokenToUser[accessToken]
    }

    private class FakeUsersRepository : UsersRepository {
        private val usersById = linkedMapOf<Long, User>()

        fun seedUser(id: Long, email: String) {
            val now = Instant.now()
            usersById[id] = User(
                id = id,
                email = email.lowercase(),
                fullName = "",
                phone = null,
                role = UserRole.TENANT,
                status = UserStatus.ACTIVE,
                passwordHash = "hash",
                preferences = Json.parseToJsonElement("{}").jsonObject,
                createdAt = now,
                updatedAt = now,
            )
        }

        fun peekUser(userId: Long): User? = usersById[userId]

        override suspend fun findByEmail(email: String): User? =
            usersById.values.firstOrNull { it.email == email.lowercase() }

        override suspend fun getById(userId: Long): User? = usersById[userId]

        override suspend fun listUsers(query: UsersListQuery): UsersPage =
            UsersPage(page = query.page, pageSize = query.pageSize, total = 0, items = emptyList())

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

    private class FakeExpensesRepository(
        private val usersRepository: FakeUsersRepository,
    ) : ExpensesRepository {
        private val expensesById = linkedMapOf<Long, StoredExpense>()
        private var nextId: Long = 1
        private var nextApprovalId: Long = 1

        fun seedExpense(
            authorId: Long,
            approverIds: List<Long>,
        ) {
            val id = nextId++
            val now = Instant.now()
            val expense = Expense(
                id = id,
                objectId = 1,
                objectAddress = "Object 1",
                authorId = authorId,
                category = "Cat",
                type = ExpenseType.FACT,
                amount = 10.0,
                expenseDate = LocalDate.parse("2026-01-10"),
                description = "Desc",
                status = ExpenseStatus.DRAFT,
                submittedAt = null,
                createdAt = now,
                updatedAt = now,
            )
            val approvals = approverIds.mapIndexed { index, approverId ->
                ExpenseApproval(
                    id = nextApprovalId++,
                    approver = usersRepository.peekUser(approverId)?.let { u ->
                        ExpenseUserSummary(id = u.id, fullName = u.fullName, email = u.email, phone = u.phone)
                    } ?: ExpenseUserSummary(id = approverId, fullName = "", email = "unknown", phone = null),
                    sortOrder = index,
                    status = ExpenseApprovalStatus.PENDING,
                    decidedAt = null,
                    comment = null,
                )
            }.toMutableList()
            expensesById[id] = StoredExpense(
                details = ExpenseDetails(
                    expense = expense,
                    approvals = approvals,
                    attachments = emptyList(),
                ),
            )
        }

        override suspend fun listForUser(user: UserContext, query: ExpensesListQuery): ExpensesPage {
            val visible = expensesById.values
                .asSequence()
                .map { it.details }
                .filter { details ->
                    details.expense.authorId == user.userId || details.approvals.any { it.approver.id == user.userId }
                }
                .filter { query.objectId == null || it.expense.objectId == query.objectId }
                .filter { query.status == null || it.expense.status == query.status }
                .filter { query.type == null || it.expense.type == query.type }
                .toList()

            return ExpensesPage(
                page = query.page,
                pageSize = query.pageSize,
                total = visible.size.toLong(),
                items = visible.map { it.expense },
            )
        }

        override suspend fun getForUser(user: UserContext, expenseId: Long): ExpenseDetails? {
            val stored = expensesById[expenseId] ?: return null
            val details = stored.details
            val visible = details.expense.authorId == user.userId || details.approvals.any { it.approver.id == user.userId }
            return if (visible) details else null
        }

        override suspend fun createForUser(user: UserContext, data: CreateExpenseData): ExpenseDetails? {
            if (data.objectId <= 0) return null
            val id = nextId++
            val expense = Expense(
                id = id,
                objectId = data.objectId,
                objectAddress = "Object ${data.objectId}",
                authorId = user.userId,
                category = data.category,
                type = data.type,
                amount = data.amount,
                expenseDate = data.expenseDate,
                description = data.description,
                status = ExpenseStatus.DRAFT,
                submittedAt = null,
                createdAt = data.now,
                updatedAt = data.now,
            )

            val approvals = data.approverIds.mapIndexed { index, approverId ->
                val approver = usersRepository.getById(approverId)
                ExpenseApproval(
                    id = nextApprovalId++,
                    approver = ExpenseUserSummary(
                        id = approverId,
                        fullName = approver?.fullName ?: "",
                        email = approver?.email ?: "unknown",
                        phone = approver?.phone,
                    ),
                    sortOrder = index,
                    status = ExpenseApprovalStatus.PENDING,
                    decidedAt = null,
                    comment = null,
                )
            }

            val stored = StoredExpense(
                details = ExpenseDetails(
                    expense = expense,
                    approvals = approvals,
                    attachments = emptyList(),
                ),
            )
            expensesById[id] = stored
            return stored.details
        }

        override suspend fun updateForAuthor(user: UserContext, expenseId: Long, patch: UpdateExpensePatch): ExpenseDetails? {
            val stored = expensesById[expenseId] ?: return null
            val current = stored.details
            if (current.expense.authorId != user.userId) return null

            val updatedExpense = current.expense.copy(
                category = patch.category ?: current.expense.category,
                type = patch.type ?: current.expense.type,
                amount = patch.amount ?: current.expense.amount,
                expenseDate = patch.expenseDate ?: current.expense.expenseDate,
                description = patch.description ?: current.expense.description,
                updatedAt = patch.now,
            )

            val updatedApprovals = patch.approverIds?.mapIndexed { index, approverId ->
                val approver = usersRepository.getById(approverId)
                ExpenseApproval(
                    id = nextApprovalId++,
                    approver = ExpenseUserSummary(
                        id = approverId,
                        fullName = approver?.fullName ?: "",
                        email = approver?.email ?: "unknown",
                        phone = approver?.phone,
                    ),
                    sortOrder = index,
                    status = ExpenseApprovalStatus.PENDING,
                    decidedAt = null,
                    comment = null,
                )
            } ?: current.approvals

            stored.details = current.copy(
                expense = updatedExpense,
                approvals = updatedApprovals,
            )
            return stored.details
        }

        override suspend fun submitForAuthor(user: UserContext, expenseId: Long, now: Instant): ExpenseDetails? {
            val stored = expensesById[expenseId] ?: return null
            val current = stored.details
            if (current.expense.authorId != user.userId) return null
            if (current.expense.status != ExpenseStatus.DRAFT) return null

            val newStatus = if (current.approvals.isEmpty()) ExpenseStatus.APPROVED else ExpenseStatus.PENDING
            stored.details = current.copy(
                expense = current.expense.copy(status = newStatus, submittedAt = now, updatedAt = now),
            )
            return stored.details
        }

        override suspend fun approve(user: UserContext, expenseId: Long, now: Instant): ExpenseDetails? {
            val stored = expensesById[expenseId] ?: return null
            val current = stored.details
            if (current.expense.status != ExpenseStatus.PENDING) return null

            val approvals = current.approvals.toMutableList()
            val idx = approvals.indexOfFirst { it.approver.id == user.userId }
            if (idx == -1) return null
            if (approvals[idx].status != ExpenseApprovalStatus.PENDING) return null

            approvals[idx] = approvals[idx].copy(status = ExpenseApprovalStatus.APPROVED, decidedAt = now)
            val allApproved = approvals.all { it.status == ExpenseApprovalStatus.APPROVED }

            stored.details = current.copy(
                expense = current.expense.copy(status = if (allApproved) ExpenseStatus.APPROVED else ExpenseStatus.PENDING, updatedAt = now),
                approvals = approvals,
            )
            return stored.details
        }

        override suspend fun reject(user: UserContext, expenseId: Long, comment: String, now: Instant): ExpenseDetails? {
            val stored = expensesById[expenseId] ?: return null
            val current = stored.details
            if (current.expense.status != ExpenseStatus.PENDING) return null

            val approvals = current.approvals.toMutableList()
            val idx = approvals.indexOfFirst { it.approver.id == user.userId }
            if (idx == -1) return null
            if (approvals[idx].status != ExpenseApprovalStatus.PENDING) return null

            approvals[idx] = approvals[idx].copy(status = ExpenseApprovalStatus.REJECTED, decidedAt = now, comment = comment)

            stored.details = current.copy(
                expense = current.expense.copy(status = ExpenseStatus.REJECTED, updatedAt = now),
                approvals = approvals,
            )
            return stored.details
        }

        override suspend fun cancelForAuthor(user: UserContext, expenseId: Long, now: Instant): ExpenseDetails? {
            val stored = expensesById[expenseId] ?: return null
            val current = stored.details
            if (current.expense.authorId != user.userId) return null

            stored.details = current.copy(
                expense = current.expense.copy(status = ExpenseStatus.CANCELED, updatedAt = now),
            )
            return stored.details
        }

        private data class StoredExpense(
            var details: ExpenseDetails,
        )
    }
}

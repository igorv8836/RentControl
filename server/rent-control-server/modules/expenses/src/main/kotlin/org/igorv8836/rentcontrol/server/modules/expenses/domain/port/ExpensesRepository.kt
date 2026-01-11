package org.igorv8836.rentcontrol.server.modules.expenses.domain.port

import org.igorv8836.rentcontrol.server.foundation.security.UserContext
import org.igorv8836.rentcontrol.server.modules.expenses.domain.model.Expense
import org.igorv8836.rentcontrol.server.modules.expenses.domain.model.ExpenseDetails
import org.igorv8836.rentcontrol.server.modules.expenses.domain.model.ExpenseStatus
import org.igorv8836.rentcontrol.server.modules.expenses.domain.model.ExpenseType
import java.time.Instant
import java.time.LocalDate

data class ExpensesListQuery(
    val search: String? = null,
    val objectId: Long? = null,
    val category: String? = null,
    val type: ExpenseType? = null,
    val status: ExpenseStatus? = null,
    val fromDate: LocalDate? = null,
    val toDate: LocalDate? = null,
    val page: Int = 1,
    val pageSize: Int = 20,
)

data class ExpensesPage(
    val page: Int,
    val pageSize: Int,
    val total: Long,
    val items: List<Expense>,
)

data class CreateExpenseData(
    val objectId: Long,
    val category: String,
    val type: ExpenseType,
    val amount: Double,
    val expenseDate: LocalDate,
    val description: String,
    val attachmentIds: List<Long>,
    val approverIds: List<Long>,
    val now: Instant,
)

data class UpdateExpensePatch(
    val category: String? = null,
    val type: ExpenseType? = null,
    val amount: Double? = null,
    val expenseDate: LocalDate? = null,
    val description: String? = null,
    val attachmentIds: List<Long>? = null,
    val approverIds: List<Long>? = null,
    val now: Instant,
)

interface ExpensesRepository {
    suspend fun listForUser(user: UserContext, query: ExpensesListQuery): ExpensesPage
    suspend fun getForUser(user: UserContext, expenseId: Long): ExpenseDetails?
    suspend fun createForUser(user: UserContext, data: CreateExpenseData): ExpenseDetails?
    suspend fun updateForAuthor(user: UserContext, expenseId: Long, patch: UpdateExpensePatch): ExpenseDetails?
    suspend fun submitForAuthor(user: UserContext, expenseId: Long, now: Instant): ExpenseDetails?
    suspend fun approve(user: UserContext, expenseId: Long, now: Instant): ExpenseDetails?
    suspend fun reject(user: UserContext, expenseId: Long, comment: String, now: Instant): ExpenseDetails?
    suspend fun cancelForAuthor(user: UserContext, expenseId: Long, now: Instant): ExpenseDetails?
}

package org.igorv8836.rentcontrol.server.modules.expenses.domain.service

import io.ktor.http.HttpStatusCode
import org.igorv8836.rentcontrol.server.foundation.errors.ApiErrorDetail
import org.igorv8836.rentcontrol.server.foundation.errors.ApiException
import org.igorv8836.rentcontrol.server.foundation.security.UserContext
import org.igorv8836.rentcontrol.server.foundation.security.UserRole
import org.igorv8836.rentcontrol.server.foundation.security.UserStatus
import org.igorv8836.rentcontrol.server.modules.expenses.domain.model.ExpenseApprovalStatus
import org.igorv8836.rentcontrol.server.modules.expenses.domain.model.ExpenseDetails
import org.igorv8836.rentcontrol.server.modules.expenses.domain.model.ExpenseStatus
import org.igorv8836.rentcontrol.server.modules.expenses.domain.model.ExpenseType
import org.igorv8836.rentcontrol.server.modules.expenses.domain.port.CreateExpenseData
import org.igorv8836.rentcontrol.server.modules.expenses.domain.port.ExpensesListQuery
import org.igorv8836.rentcontrol.server.modules.expenses.domain.port.ExpensesPage
import org.igorv8836.rentcontrol.server.modules.expenses.domain.port.ExpensesRepository
import org.igorv8836.rentcontrol.server.modules.expenses.domain.port.UpdateExpensePatch
import org.igorv8836.rentcontrol.server.modules.users.domain.port.UsersRepository
import java.time.Instant
import java.time.LocalDate

class ExpensesService(
    private val expensesRepository: ExpensesRepository,
    private val usersRepository: UsersRepository,
) {
    suspend fun listExpenses(user: UserContext, query: ExpensesListQuery): ExpensesPage {
        requireValidPaging(query.page, query.pageSize)
        return expensesRepository.listForUser(user, query)
    }

    suspend fun getExpense(user: UserContext, expenseId: Long): ExpenseDetails {
        return expensesRepository.getForUser(user, expenseId)
            ?: throw ApiException(
                status = HttpStatusCode.NotFound,
                code = "not_found",
                message = "Expense not found",
            )
    }

    suspend fun createExpense(
        user: UserContext,
        objectId: Long,
        category: String,
        type: ExpenseType,
        amount: Double,
        expenseDate: LocalDate,
        description: String,
        attachmentIds: List<Long>,
        approverIds: List<Long>,
        now: Instant = Instant.now(),
    ): ExpenseDetails {
        val normalizedCategory = category.trim()
        if (normalizedCategory.isBlank()) {
            throw ApiException(
                status = HttpStatusCode.BadRequest,
                code = "validation_error",
                message = "Invalid category",
                details = listOf(ApiErrorDetail(field = "category", issue = "blank")),
            )
        }

        val normalizedDescription = description.trim()
        if (normalizedDescription.isBlank()) {
            throw ApiException(
                status = HttpStatusCode.BadRequest,
                code = "validation_error",
                message = "Invalid description",
                details = listOf(ApiErrorDetail(field = "description", issue = "blank")),
            )
        }

        if (amount <= 0.0) {
            throw ApiException(
                status = HttpStatusCode.BadRequest,
                code = "validation_error",
                message = "Invalid amount",
                details = listOf(ApiErrorDetail(field = "amount", issue = "must_be_positive")),
            )
        }

        val distinctApproverIds = approverIds.distinct()
        if (distinctApproverIds.contains(user.userId)) {
            throw ApiException(
                status = HttpStatusCode.BadRequest,
                code = "validation_error",
                message = "Invalid approverIds",
                details = listOf(ApiErrorDetail(field = "approverIds", issue = "cannot_contain_author")),
            )
        }

        validateApprovers(distinctApproverIds)

        val created = expensesRepository.createForUser(
            user = user,
            data = CreateExpenseData(
                objectId = objectId,
                category = normalizedCategory,
                type = type,
                amount = amount,
                expenseDate = expenseDate,
                description = normalizedDescription,
                attachmentIds = attachmentIds.distinct(),
                approverIds = distinctApproverIds,
                now = now,
            ),
        )

        return created
            ?: throw ApiException(
                status = HttpStatusCode.NotFound,
                code = "not_found",
                message = "Object not found",
            )
    }

    suspend fun updateExpense(
        user: UserContext,
        expenseId: Long,
        patch: UpdateExpensePatchInput,
        now: Instant = Instant.now(),
    ): ExpenseDetails {
        val existing = getExpense(user, expenseId)
        if (existing.expense.authorId != user.userId) {
            throw ApiException(
                status = HttpStatusCode.NotFound,
                code = "not_found",
                message = "Expense not found",
            )
        }

        when (existing.expense.status) {
            ExpenseStatus.DRAFT -> Unit
            ExpenseStatus.PENDING -> {
                if (existing.approvals.any { it.status != ExpenseApprovalStatus.PENDING }) {
                    throw ApiException(
                        status = HttpStatusCode.Conflict,
                        code = "expense_locked",
                        message = "Expense cannot be edited after approvals started",
                    )
                }
            }

            ExpenseStatus.APPROVED, ExpenseStatus.REJECTED, ExpenseStatus.CANCELED -> throw ApiException(
                status = HttpStatusCode.Conflict,
                code = "expense_locked",
                message = "Expense cannot be edited in current status",
            )
        }

        patch.category?.let {
            if (it.isBlank()) {
                throw ApiException(
                    status = HttpStatusCode.BadRequest,
                    code = "validation_error",
                    message = "Invalid category",
                    details = listOf(ApiErrorDetail(field = "category", issue = "blank")),
                )
            }
        }

        patch.description?.let {
            if (it.isBlank()) {
                throw ApiException(
                    status = HttpStatusCode.BadRequest,
                    code = "validation_error",
                    message = "Invalid description",
                    details = listOf(ApiErrorDetail(field = "description", issue = "blank")),
                )
            }
        }

        patch.amount?.let {
            if (it <= 0.0) {
                throw ApiException(
                    status = HttpStatusCode.BadRequest,
                    code = "validation_error",
                    message = "Invalid amount",
                    details = listOf(ApiErrorDetail(field = "amount", issue = "must_be_positive")),
                )
            }
        }

        val approverIds = patch.approverIds?.distinct()
        if (approverIds != null) {
            if (existing.expense.status != ExpenseStatus.DRAFT) {
                throw ApiException(
                    status = HttpStatusCode.Conflict,
                    code = "expense_locked",
                    message = "Approvers can be changed only in draft",
                )
            }
            if (approverIds.contains(user.userId)) {
                throw ApiException(
                    status = HttpStatusCode.BadRequest,
                    code = "validation_error",
                    message = "Invalid approverIds",
                    details = listOf(ApiErrorDetail(field = "approverIds", issue = "cannot_contain_author")),
                )
            }
            validateApprovers(approverIds)
        }

        val updated = expensesRepository.updateForAuthor(
            user = user,
            expenseId = expenseId,
            patch = UpdateExpensePatch(
                category = patch.category?.trim(),
                type = patch.type,
                amount = patch.amount,
                expenseDate = patch.expenseDate,
                description = patch.description?.trim(),
                attachmentIds = patch.attachmentIds?.distinct(),
                approverIds = approverIds,
                now = now,
            ),
        )

        return updated
            ?: throw ApiException(
                status = HttpStatusCode.NotFound,
                code = "not_found",
                message = "Expense not found",
            )
    }

    suspend fun submitExpense(user: UserContext, expenseId: Long, now: Instant = Instant.now()): ExpenseDetails {
        val existing = getExpense(user, expenseId)
        if (existing.expense.authorId != user.userId) {
            throw ApiException(
                status = HttpStatusCode.NotFound,
                code = "not_found",
                message = "Expense not found",
            )
        }
        if (existing.expense.status != ExpenseStatus.DRAFT) {
            throw ApiException(
                status = HttpStatusCode.Conflict,
                code = "invalid_state",
                message = "Only draft expense can be submitted",
            )
        }

        val submitted = expensesRepository.submitForAuthor(user, expenseId, now)
        return submitted
            ?: throw ApiException(
                status = HttpStatusCode.NotFound,
                code = "not_found",
                message = "Expense not found",
            )
    }

    suspend fun approveExpense(user: UserContext, expenseId: Long, now: Instant = Instant.now()): ExpenseDetails {
        val existing = getExpense(user, expenseId)
        if (existing.expense.status != ExpenseStatus.PENDING) {
            throw ApiException(
                status = HttpStatusCode.Conflict,
                code = "invalid_state",
                message = "Expense is not pending",
            )
        }
        if (existing.approvals.none { it.approver.id == user.userId }) {
            throw ApiException(
                status = HttpStatusCode.NotFound,
                code = "not_found",
                message = "Expense not found",
            )
        }

        return expensesRepository.approve(user, expenseId, now)
            ?: throw ApiException(
                status = HttpStatusCode.Conflict,
                code = "invalid_state",
                message = "Approval is not allowed",
            )
    }

    suspend fun rejectExpense(user: UserContext, expenseId: Long, comment: String, now: Instant = Instant.now()): ExpenseDetails {
        val normalizedComment = comment.trim()
        if (normalizedComment.isBlank()) {
            throw ApiException(
                status = HttpStatusCode.BadRequest,
                code = "validation_error",
                message = "Invalid comment",
                details = listOf(ApiErrorDetail(field = "comment", issue = "blank")),
            )
        }

        val existing = getExpense(user, expenseId)
        if (existing.expense.status != ExpenseStatus.PENDING) {
            throw ApiException(
                status = HttpStatusCode.Conflict,
                code = "invalid_state",
                message = "Expense is not pending",
            )
        }
        if (existing.approvals.none { it.approver.id == user.userId }) {
            throw ApiException(
                status = HttpStatusCode.NotFound,
                code = "not_found",
                message = "Expense not found",
            )
        }

        return expensesRepository.reject(user, expenseId, normalizedComment, now)
            ?: throw ApiException(
                status = HttpStatusCode.Conflict,
                code = "invalid_state",
                message = "Rejection is not allowed",
            )
    }

    suspend fun cancelExpense(user: UserContext, expenseId: Long, now: Instant = Instant.now()): ExpenseDetails {
        val existing = getExpense(user, expenseId)
        if (existing.expense.authorId != user.userId) {
            throw ApiException(
                status = HttpStatusCode.NotFound,
                code = "not_found",
                message = "Expense not found",
            )
        }
        if (existing.expense.status == ExpenseStatus.APPROVED || existing.expense.status == ExpenseStatus.CANCELED) {
            throw ApiException(
                status = HttpStatusCode.Conflict,
                code = "invalid_state",
                message = "Expense cannot be canceled",
            )
        }

        return expensesRepository.cancelForAuthor(user, expenseId, now)
            ?: throw ApiException(
                status = HttpStatusCode.NotFound,
                code = "not_found",
                message = "Expense not found",
            )
    }

    private suspend fun validateApprovers(approverIds: List<Long>) {
        for (id in approverIds) {
            val user = usersRepository.getById(id)
                ?: throw ApiException(
                    status = HttpStatusCode.BadRequest,
                    code = "validation_error",
                    message = "Unknown approverId",
                    details = listOf(ApiErrorDetail(field = "approverIds", issue = "unknown_user")),
                )
            if (user.status != UserStatus.ACTIVE) {
                throw ApiException(
                    status = HttpStatusCode.BadRequest,
                    code = "validation_error",
                    message = "Invalid approver",
                    details = listOf(ApiErrorDetail(field = "approverIds", issue = "inactive_user")),
                )
            }
            if (user.role == UserRole.TENANT) {
                // allowed, but keep explicit for future restrictions
                Unit
            }
        }
    }

    private fun requireValidPaging(page: Int, pageSize: Int) {
        if (page < 1) {
            throw ApiException(
                status = HttpStatusCode.BadRequest,
                code = "invalid_argument",
                message = "page must be >= 1",
            )
        }
        if (pageSize !in 1..100) {
            throw ApiException(
                status = HttpStatusCode.BadRequest,
                code = "invalid_argument",
                message = "pageSize must be in 1..100",
            )
        }
    }
}

data class UpdateExpensePatchInput(
    val category: String? = null,
    val type: ExpenseType? = null,
    val amount: Double? = null,
    val expenseDate: LocalDate? = null,
    val description: String? = null,
    val attachmentIds: List<Long>? = null,
    val approverIds: List<Long>? = null,
)

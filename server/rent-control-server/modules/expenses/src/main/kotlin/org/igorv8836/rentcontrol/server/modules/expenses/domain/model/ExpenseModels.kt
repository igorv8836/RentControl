package org.igorv8836.rentcontrol.server.modules.expenses.domain.model

import java.time.Instant
import java.time.LocalDate

data class Expense(
    val id: Long,
    val objectId: Long,
    val objectAddress: String,
    val authorId: Long,
    val category: String,
    val type: ExpenseType,
    val amount: Double,
    val expenseDate: LocalDate,
    val description: String,
    val status: ExpenseStatus,
    val submittedAt: Instant?,
    val createdAt: Instant,
    val updatedAt: Instant,
)

data class ExpenseUserSummary(
    val id: Long,
    val fullName: String,
    val email: String,
    val phone: String?,
)

data class ExpenseApproval(
    val id: Long,
    val approver: ExpenseUserSummary,
    val sortOrder: Int,
    val status: ExpenseApprovalStatus,
    val decidedAt: Instant?,
    val comment: String?,
)

data class ExpenseAttachment(
    val id: Long,
    val url: String,
    val type: String,
    val createdAt: Instant,
)

data class ExpenseDetails(
    val expense: Expense,
    val approvals: List<ExpenseApproval>,
    val attachments: List<ExpenseAttachment>,
)


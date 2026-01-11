package org.igorv8836.rentcontrol.server.modules.expenses.api.dto

import kotlinx.serialization.Serializable
import org.igorv8836.rentcontrol.server.modules.expenses.domain.model.ExpenseApprovalStatus
import org.igorv8836.rentcontrol.server.modules.expenses.domain.model.ExpenseStatus
import org.igorv8836.rentcontrol.server.modules.expenses.domain.model.ExpenseType

@Serializable
data class ExpensesListResponse(
    val page: Int,
    val pageSize: Int,
    val total: Long,
    val items: List<ExpenseListItem>,
)

@Serializable
data class ExpenseListItem(
    val id: Long,
    val objectId: Long,
    val objectAddress: String,
    val category: String,
    val type: ExpenseType,
    val amount: Double,
    val expenseDate: String,
    val description: String,
    val status: ExpenseStatus,
)

@Serializable
data class ExpenseDetailsResponse(
    val id: Long,
    val objectId: Long,
    val objectAddress: String,
    val authorId: Long,
    val category: String,
    val type: ExpenseType,
    val amount: Double,
    val expenseDate: String,
    val description: String,
    val status: ExpenseStatus,
    val submittedAt: String? = null,
    val approvals: List<ExpenseApprovalItem>,
    val attachments: List<ExpenseAttachmentItem>,
)

@Serializable
data class ExpenseApprovalItem(
    val id: Long,
    val approver: ExpenseUserSummary,
    val sortOrder: Int,
    val status: ExpenseApprovalStatus,
    val decidedAt: String? = null,
    val comment: String? = null,
)

@Serializable
data class ExpenseUserSummary(
    val id: Long,
    val fullName: String,
    val email: String,
    val phone: String? = null,
)

@Serializable
data class ExpenseAttachmentItem(
    val id: Long,
    val url: String,
    val type: String,
    val createdAt: String,
)

@Serializable
data class CreateExpenseRequest(
    val objectId: Long,
    val category: String,
    val type: ExpenseType,
    val amount: Double,
    val expenseDate: String,
    val description: String,
    val attachmentIds: List<Long> = emptyList(),
    val approverIds: List<Long> = emptyList(),
)

@Serializable
data class UpdateExpenseRequest(
    val category: String? = null,
    val type: ExpenseType? = null,
    val amount: Double? = null,
    val expenseDate: String? = null,
    val description: String? = null,
    val attachmentIds: List<Long>? = null,
    val approverIds: List<Long>? = null,
)

@Serializable
data class RejectExpenseRequest(
    val comment: String,
)


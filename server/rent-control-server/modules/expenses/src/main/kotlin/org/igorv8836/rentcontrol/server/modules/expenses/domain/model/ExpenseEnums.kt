package org.igorv8836.rentcontrol.server.modules.expenses.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ExpenseType {
    @SerialName("plan")
    PLAN,

    @SerialName("fact")
    FACT,
}

fun ExpenseType.toDbValue(): String = when (this) {
    ExpenseType.PLAN -> "plan"
    ExpenseType.FACT -> "fact"
}

fun expenseTypeFromDb(value: String): ExpenseType = when (value) {
    "plan" -> ExpenseType.PLAN
    "fact" -> ExpenseType.FACT
    else -> throw IllegalArgumentException("Unknown expense type: $value")
}

@Serializable
enum class ExpenseStatus {
    @SerialName("draft")
    DRAFT,

    @SerialName("pending")
    PENDING,

    @SerialName("approved")
    APPROVED,

    @SerialName("rejected")
    REJECTED,

    @SerialName("canceled")
    CANCELED,
}

fun ExpenseStatus.toDbValue(): String = when (this) {
    ExpenseStatus.DRAFT -> "draft"
    ExpenseStatus.PENDING -> "pending"
    ExpenseStatus.APPROVED -> "approved"
    ExpenseStatus.REJECTED -> "rejected"
    ExpenseStatus.CANCELED -> "canceled"
}

fun expenseStatusFromDb(value: String): ExpenseStatus = when (value) {
    "draft" -> ExpenseStatus.DRAFT
    "pending" -> ExpenseStatus.PENDING
    "approved" -> ExpenseStatus.APPROVED
    "rejected" -> ExpenseStatus.REJECTED
    "canceled" -> ExpenseStatus.CANCELED
    else -> throw IllegalArgumentException("Unknown expense status: $value")
}

@Serializable
enum class ExpenseApprovalStatus {
    @SerialName("pending")
    PENDING,

    @SerialName("approved")
    APPROVED,

    @SerialName("rejected")
    REJECTED,
}

fun ExpenseApprovalStatus.toDbValue(): String = when (this) {
    ExpenseApprovalStatus.PENDING -> "pending"
    ExpenseApprovalStatus.APPROVED -> "approved"
    ExpenseApprovalStatus.REJECTED -> "rejected"
}

fun expenseApprovalStatusFromDb(value: String): ExpenseApprovalStatus = when (value) {
    "pending" -> ExpenseApprovalStatus.PENDING
    "approved" -> ExpenseApprovalStatus.APPROVED
    "rejected" -> ExpenseApprovalStatus.REJECTED
    else -> throw IllegalArgumentException("Unknown expense approval status: $value")
}


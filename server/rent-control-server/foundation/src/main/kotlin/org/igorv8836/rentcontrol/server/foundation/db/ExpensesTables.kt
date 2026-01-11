package org.igorv8836.rentcontrol.server.foundation.db

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.CurrentTimestampWithTimeZone
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone

object ExpensesTable : LongIdTable("expenses") {
    val propertyId = reference("property_id", PropertiesTable, onDelete = ReferenceOption.CASCADE)
    val authorId = reference("author_id", UsersTable)
    val category = text("category")
    val type = varchar("type", 16)
    val amount = decimal("amount", 12, 2)
    val expenseDate = date("expense_date")
    val description = text("description")
    val status = varchar("status", 16)
    val submittedAt = timestampWithTimeZone("submitted_at").nullable()
    val createdAt = timestampWithTimeZone("created_at").defaultExpression(CurrentTimestampWithTimeZone)
    val updatedAt = timestampWithTimeZone("updated_at").defaultExpression(CurrentTimestampWithTimeZone)

    init {
        check("expenses_type_check") { type inList listOf("plan", "fact") }
        check("expenses_status_check") { status inList listOf("draft", "pending", "approved", "rejected", "canceled") }
        index("idx_expenses_property_id", false, propertyId)
        index("idx_expenses_author_id", false, authorId)
        index("idx_expenses_status", false, status)
        index("idx_expenses_expense_date", false, expenseDate)
    }
}

object ExpenseApprovalsTable : LongIdTable("expense_approvals") {
    val expenseId = reference("expense_id", ExpensesTable, onDelete = ReferenceOption.CASCADE)
    val approverId = reference("approver_id", UsersTable)
    val sortOrder = integer("sort_order")
    val status = varchar("status", 16)
    val decidedAt = timestampWithTimeZone("decided_at").nullable()
    val comment = text("comment").nullable()
    val createdAt = timestampWithTimeZone("created_at").defaultExpression(CurrentTimestampWithTimeZone)

    init {
        uniqueIndex("ux_expense_approvals_expense_approver", expenseId, approverId)
        index("idx_expense_approvals_expense_id", false, expenseId)
        index("idx_expense_approvals_approver_id", false, approverId)
        check("expense_approvals_status_check") { status inList listOf("pending", "approved", "rejected") }
    }
}


package org.igorv8836.rentcontrol.server.foundation.db

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.CurrentTimestampWithTimeZone
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone
import java.math.BigDecimal

object InvoicesTable : LongIdTable("invoices") {
    val propertyId = reference("property_id", PropertiesTable)
    val tenantId = reference("tenant_id", UsersTable)
    val actId = reference("act_id", ActsTable, onDelete = ReferenceOption.SET_NULL).nullable()
    val totalAmount = decimal("total_amount", 12, 2)
    val status = text("status")
    val issueDate = date("issue_date")
    val dueDate = date("due_date").nullable()
    val paymentLink = text("payment_link").nullable()
    val createdAt = timestampWithTimeZone("created_at").defaultExpression(CurrentTimestampWithTimeZone)
    val updatedAt = timestampWithTimeZone("updated_at").defaultExpression(CurrentTimestampWithTimeZone)

    init {
        index("idx_invoices_property_id", false, propertyId)
        index("idx_invoices_tenant_id", false, tenantId)
    }
}

object InvoiceItemsTable : LongIdTable("invoice_items") {
    val invoiceId = reference("invoice_id", InvoicesTable, onDelete = ReferenceOption.CASCADE)
    val description = text("description")
    val quantity = decimal("quantity", 12, 2).default(BigDecimal.ONE)
    val price = decimal("price", 12, 2)
    val amount = decimal("amount", 12, 2)

    init {
        index("idx_invoice_items_invoice_id", false, invoiceId)
    }
}

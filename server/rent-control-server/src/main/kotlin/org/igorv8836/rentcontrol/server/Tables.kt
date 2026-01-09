package org.igorv8836.rentcontrol.server

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.CurrentTimestampWithTimeZone
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.sql.json.jsonb
import java.math.BigDecimal

private val jsonFormat = Json { ignoreUnknownKeys = true }

object UsersTable : LongIdTable("users") {
    val fullName = text("full_name")
    val email = text("email").uniqueIndex()
    val phone = text("phone").nullable()
    val role = varchar("role", 32)
    val status = varchar("status", 32)
    val passwordHash = text("password_hash")
    val preferences = jsonb("preferences", jsonFormat, JsonObject.serializer())
        .default(JsonObject(emptyMap()))
    val createdAt = timestampWithTimeZone("created_at").defaultExpression(CurrentTimestampWithTimeZone)
    val updatedAt = timestampWithTimeZone("updated_at").defaultExpression(CurrentTimestampWithTimeZone)

    init {
        check("users_role_check") { role inList listOf("admin", "landlord", "inspector", "tenant") }
        check("users_status_check") { status inList listOf("active", "blocked") }
    }
}

object PropertiesTable : LongIdTable("properties") {
    val address = text("address")
    val type = text("type")
    val area = decimal("area", 12, 2).nullable()
    val status = varchar("status", 32)
    val ownerId = reference("owner_id", UsersTable)
    val tenantId = reference("tenant_id", UsersTable, onDelete = ReferenceOption.SET_NULL).nullable()
    val notes = text("notes").nullable()
    val createdAt = timestampWithTimeZone("created_at").defaultExpression(CurrentTimestampWithTimeZone)
    val updatedAt = timestampWithTimeZone("updated_at").defaultExpression(CurrentTimestampWithTimeZone)

    init {
        check("properties_status_check") { status inList listOf("available", "leased") }
        index("idx_properties_owner_id", false, ownerId)
        index("idx_properties_tenant_id", false, tenantId)
    }
}

object LeasesTable : LongIdTable("leases") {
    val propertyId = reference("property_id", PropertiesTable)
    val landlordId = reference("landlord_id", UsersTable)
    val tenantId = reference("tenant_id", UsersTable)
    val startDate = date("start_date")
    val endDate = date("end_date").nullable()
    val rentAmount = decimal("rent_amount", 12, 2)
    val depositAmount = decimal("deposit_amount", 12, 2).nullable()
    val status = text("status")
    val createdAt = timestampWithTimeZone("created_at").defaultExpression(CurrentTimestampWithTimeZone)
    val updatedAt = timestampWithTimeZone("updated_at").defaultExpression(CurrentTimestampWithTimeZone)

    init {
        index("idx_leases_property_id", false, propertyId)
        index("idx_leases_tenant_id", false, tenantId)
    }
}

object InspectionsTable : LongIdTable("inspections") {
    val propertyId = reference("property_id", PropertiesTable)
    val inspectorId = reference("inspector_id", UsersTable)
    val scheduledDate = timestampWithTimeZone("scheduled_date").nullable()
    val completedDate = timestampWithTimeZone("completed_date").nullable()
    val status = varchar("status", 32)
    val priority = varchar("priority", 32).nullable()
    val comments = text("comments").nullable()
    val createdAt = timestampWithTimeZone("created_at").defaultExpression(CurrentTimestampWithTimeZone)
    val updatedAt = timestampWithTimeZone("updated_at").defaultExpression(CurrentTimestampWithTimeZone)

    init {
        check("inspections_status_check") { status inList listOf("scheduled", "completed", "approved") }
        index("idx_inspections_property_id", false, propertyId)
        index("idx_inspections_inspector_id", false, inspectorId)
    }
}

object ChecklistTemplatesTable : LongIdTable("checklist_templates") {
    val name = text("name")
    val propertyType = text("property_type").nullable()
    val createdBy = reference("created_by", UsersTable, onDelete = ReferenceOption.SET_NULL).nullable()
    val createdAt = timestampWithTimeZone("created_at").defaultExpression(CurrentTimestampWithTimeZone)
    val updatedAt = timestampWithTimeZone("updated_at").defaultExpression(CurrentTimestampWithTimeZone)
}

object ChecklistItemsTable : LongIdTable("checklist_items") {
    val templateId = reference("template_id", ChecklistTemplatesTable, onDelete = ReferenceOption.CASCADE)
    val title = text("title")
    val description = text("description").nullable()
    val sortOrder = integer("sort_order").default(0)
    val createdAt = timestampWithTimeZone("created_at").defaultExpression(CurrentTimestampWithTimeZone)
    val updatedAt = timestampWithTimeZone("updated_at").defaultExpression(CurrentTimestampWithTimeZone)
}

object InspectionItemsTable : LongIdTable("inspection_items") {
    val inspectionId = reference("inspection_id", InspectionsTable, onDelete = ReferenceOption.CASCADE)
    val checklistItemId = reference("checklist_item_id", ChecklistItemsTable)
    val status = text("status").nullable()
    val comment = text("comment").nullable()
    val createdAt = timestampWithTimeZone("created_at").defaultExpression(CurrentTimestampWithTimeZone)
    val updatedAt = timestampWithTimeZone("updated_at").defaultExpression(CurrentTimestampWithTimeZone)
}

object DefectsTable : LongIdTable("defects") {
    val propertyId = reference("property_id", PropertiesTable)
    val inspectionId = reference("inspection_id", InspectionsTable, onDelete = ReferenceOption.SET_NULL).nullable()
    val category = text("category").nullable()
    val description = text("description")
    val priority = text("priority").nullable()
    val deadline = timestampWithTimeZone("deadline").nullable()
    val assigneeId = reference("assignee_id", UsersTable, onDelete = ReferenceOption.SET_NULL).nullable()
    val status = text("status")
    val createdAt = timestampWithTimeZone("created_at").defaultExpression(CurrentTimestampWithTimeZone)
    val resolvedAt = timestampWithTimeZone("resolved_at").nullable()
    val updatedAt = timestampWithTimeZone("updated_at").defaultExpression(CurrentTimestampWithTimeZone)

    init {
        index("idx_defects_property_id", false, propertyId)
        index("idx_defects_assignee_id", false, assigneeId)
    }
}

object ActsTable : LongIdTable("acts") {
    val inspectionId = reference("inspection_id", InspectionsTable)
    val version = integer("version").default(1)
    val pdfUrl = text("pdf_url")
    val signedBy = reference("signed_by", UsersTable).nullable()
    val signedAt = timestampWithTimeZone("signed_at").nullable()
    val createdAt = timestampWithTimeZone("created_at").defaultExpression(CurrentTimestampWithTimeZone)
}

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

object MeterReadingsTable : LongIdTable("meter_readings") {
    val propertyId = reference("property_id", PropertiesTable)
    val invoiceId = reference("invoice_id", InvoicesTable, onDelete = ReferenceOption.SET_NULL).nullable()
    val type = text("type")
    val meterNumber = text("meter_number")
    val previousReading = decimal("previous_reading", 18, 4).nullable()
    val currentReading = decimal("current_reading", 18, 4).nullable()
    val difference = decimal("difference", 18, 4).nullable()
    val tariff = decimal("tariff", 12, 4).nullable()
    val amount = decimal("amount", 12, 2).nullable()
    val recordedAt = timestampWithTimeZone("recorded_at").defaultExpression(CurrentTimestampWithTimeZone)
    val recordedBy = reference("recorded_by", UsersTable, onDelete = ReferenceOption.SET_NULL).nullable()

    init {
        index("idx_meter_readings_property_id", false, propertyId)
    }
}

object MediaFilesTable : LongIdTable("media_files") {
    val ownerId = reference("owner_id", UsersTable, onDelete = ReferenceOption.SET_NULL).nullable()
    val entityType = text("entity_type")
    val entityId = long("entity_id")
    val fileUrl = text("file_url")
    val fileType = text("file_type")
    val createdAt = timestampWithTimeZone("created_at").defaultExpression(CurrentTimestampWithTimeZone)
}

object NotificationsTable : LongIdTable("notifications") {
    val userId = reference("user_id", UsersTable)
    val type = text("type")
    val title = text("title")
    val body = text("body")
    val status = text("status")
    val createdAt = timestampWithTimeZone("created_at").defaultExpression(CurrentTimestampWithTimeZone)
}

object AuditLogTable : LongIdTable("audit_log") {
    val userId = reference("user_id", UsersTable, onDelete = ReferenceOption.SET_NULL).nullable()
    val entityType = text("entity_type")
    val entityId = long("entity_id")
    val action = text("action")
    val createdAt = timestampWithTimeZone("created_at").defaultExpression(CurrentTimestampWithTimeZone)
}

val allTables = arrayOf(
    UsersTable,
    PropertiesTable,
    LeasesTable,
    InspectionsTable,
    ChecklistTemplatesTable,
    ChecklistItemsTable,
    InspectionItemsTable,
    DefectsTable,
    ActsTable,
    InvoicesTable,
    InvoiceItemsTable,
    MeterReadingsTable,
    MediaFilesTable,
    NotificationsTable,
    AuditLogTable,
)

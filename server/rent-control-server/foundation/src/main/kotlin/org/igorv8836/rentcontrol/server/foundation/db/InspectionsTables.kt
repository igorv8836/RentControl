package org.igorv8836.rentcontrol.server.foundation.db

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.CurrentTimestampWithTimeZone
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone

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

object ActsTable : LongIdTable("acts") {
    val inspectionId = reference("inspection_id", InspectionsTable)
    val version = integer("version").default(1)
    val pdfUrl = text("pdf_url")
    val signedBy = reference("signed_by", UsersTable).nullable()
    val signedAt = timestampWithTimeZone("signed_at").nullable()
    val createdAt = timestampWithTimeZone("created_at").defaultExpression(CurrentTimestampWithTimeZone)
}

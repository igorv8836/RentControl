package org.igorv8836.rentcontrol.server.foundation.db

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.CurrentTimestampWithTimeZone
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone

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

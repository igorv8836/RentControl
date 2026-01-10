package org.igorv8836.rentcontrol.server.foundation.db

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.CurrentTimestampWithTimeZone
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone

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

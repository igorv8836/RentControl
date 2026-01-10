package org.igorv8836.rentcontrol.server.foundation.db

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.CurrentTimestampWithTimeZone
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone

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

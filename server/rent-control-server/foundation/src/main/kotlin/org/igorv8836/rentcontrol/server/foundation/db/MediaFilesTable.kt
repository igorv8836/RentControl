package org.igorv8836.rentcontrol.server.foundation.db

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.CurrentTimestampWithTimeZone
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone

object MediaFilesTable : LongIdTable("media_files") {
    val ownerId = reference("owner_id", UsersTable, onDelete = ReferenceOption.SET_NULL).nullable()
    val entityType = text("entity_type")
    val entityId = long("entity_id")
    val fileUrl = text("file_url")
    val fileType = text("file_type")
    val createdAt = timestampWithTimeZone("created_at").defaultExpression(CurrentTimestampWithTimeZone)
}

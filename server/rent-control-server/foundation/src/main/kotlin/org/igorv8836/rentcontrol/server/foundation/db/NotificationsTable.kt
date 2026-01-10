package org.igorv8836.rentcontrol.server.foundation.db

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.CurrentTimestampWithTimeZone
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone

object NotificationsTable : LongIdTable("notifications") {
    val userId = reference("user_id", UsersTable)
    val type = text("type")
    val title = text("title")
    val body = text("body")
    val status = text("status")
    val createdAt = timestampWithTimeZone("created_at").defaultExpression(CurrentTimestampWithTimeZone)
}

package org.igorv8836.rentcontrol.server.foundation.db

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.CurrentTimestampWithTimeZone
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.sql.json.jsonb

private val jsonFormat = Json { ignoreUnknownKeys = true }

object UsersTable : LongIdTable("users") {
    val fullName = text("full_name").default("")
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
        check("users_status_check") { status inList listOf("pending", "active", "blocked") }
    }
}

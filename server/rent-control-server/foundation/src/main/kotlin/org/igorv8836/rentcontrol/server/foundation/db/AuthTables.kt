package org.igorv8836.rentcontrol.server.foundation.db

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.CurrentTimestampWithTimeZone
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone

object AuthSessionsTable : LongIdTable("auth_sessions") {
    val userId = reference("user_id", UsersTable)
    val refreshTokenHash = text("refresh_token_hash").uniqueIndex()
    val refreshExpiresAt = timestampWithTimeZone("refresh_expires_at")
    val accessTokenHash = text("access_token_hash").uniqueIndex()
    val accessExpiresAt = timestampWithTimeZone("access_expires_at")
    val revokedAt = timestampWithTimeZone("revoked_at").nullable()
    val createdAt = timestampWithTimeZone("created_at").defaultExpression(CurrentTimestampWithTimeZone)
    val updatedAt = timestampWithTimeZone("updated_at").defaultExpression(CurrentTimestampWithTimeZone)

    init {
        index("idx_auth_sessions_user_id", false, userId)
        index("idx_auth_sessions_access_expires_at", false, accessExpiresAt)
        index("idx_auth_sessions_refresh_expires_at", false, refreshExpiresAt)
    }
}

object OtpCodesTable : LongIdTable("otp_codes") {
    val email = text("email")
    val userId = reference("user_id", UsersTable, onDelete = ReferenceOption.CASCADE).nullable()
    val purpose = varchar("purpose", 32)
    val codeHash = text("code_hash")
    val expiresAt = timestampWithTimeZone("expires_at")
    val consumedAt = timestampWithTimeZone("consumed_at").nullable()
    val attempts = integer("attempts").default(0)
    val createdAt = timestampWithTimeZone("created_at").defaultExpression(CurrentTimestampWithTimeZone)

    init {
        index("idx_otp_codes_email", false, email)
        index("idx_otp_codes_user_id", false, userId)
        index("idx_otp_codes_expires_at", false, expiresAt)
    }
}

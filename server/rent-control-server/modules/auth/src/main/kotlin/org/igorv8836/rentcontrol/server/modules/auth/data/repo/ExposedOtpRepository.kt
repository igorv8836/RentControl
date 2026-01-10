package org.igorv8836.rentcontrol.server.modules.auth.data.repo

import org.igorv8836.rentcontrol.server.foundation.db.OtpCodesTable
import org.igorv8836.rentcontrol.server.foundation.db.UsersTable
import org.igorv8836.rentcontrol.server.modules.auth.domain.model.OtpPurpose
import org.igorv8836.rentcontrol.server.modules.auth.domain.model.toDbValue
import org.igorv8836.rentcontrol.server.modules.auth.domain.port.OtpConsumeResult
import org.igorv8836.rentcontrol.server.modules.auth.domain.port.OtpRepository
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

class ExposedOtpRepository(
    private val database: Database,
) : OtpRepository {
    override suspend fun createOtp(
        email: String,
        userId: Long?,
        purpose: OtpPurpose,
        codeHash: String,
        expiresAt: Instant,
    ) {
        newSuspendedTransaction(db = database) {
            OtpCodesTable.insert { row ->
                row[OtpCodesTable.email] = email.lowercase()
                row[OtpCodesTable.userId] = userId?.let { EntityID(it, UsersTable) }
                row[OtpCodesTable.purpose] = purpose.toDbValue()
                row[OtpCodesTable.codeHash] = codeHash
                row[OtpCodesTable.expiresAt] = expiresAt.toOffsetDateTime()
            }
        }
    }

    override suspend fun consumeOtp(
        email: String,
        purpose: OtpPurpose,
        codeHash: String,
        now: Instant,
    ): OtpConsumeResult = newSuspendedTransaction(db = database) {
        val record = OtpCodesTable
            .selectAll()
            .where {
                (OtpCodesTable.email eq email.lowercase()) and
                    (OtpCodesTable.purpose eq purpose.toDbValue()) and
                    (OtpCodesTable.consumedAt eq null)
            }
            .orderBy(OtpCodesTable.id, SortOrder.DESC)
            .limit(1)
            .singleOrNull()
            ?: return@newSuspendedTransaction OtpConsumeResult.Invalid

        val nowOffset = now.toOffsetDateTime()
        if (record[OtpCodesTable.expiresAt] <= nowOffset) {
            return@newSuspendedTransaction OtpConsumeResult.Expired
        }

        if (record[OtpCodesTable.codeHash] != codeHash) {
            OtpCodesTable.update({ OtpCodesTable.id eq record[OtpCodesTable.id] }) { row ->
                row[OtpCodesTable.attempts] = record[OtpCodesTable.attempts] + 1
            }
            return@newSuspendedTransaction OtpConsumeResult.Invalid
        }

        OtpCodesTable.update({ OtpCodesTable.id eq record[OtpCodesTable.id] }) { row ->
            row[OtpCodesTable.consumedAt] = nowOffset
        }

        OtpConsumeResult.Success(userId = record[OtpCodesTable.userId]?.value)
    }

    private fun Instant.toOffsetDateTime(): OffsetDateTime = OffsetDateTime.ofInstant(this, ZoneOffset.UTC)
}

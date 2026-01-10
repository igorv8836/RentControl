package org.igorv8836.rentcontrol.server.modules.auth.domain.port

import org.igorv8836.rentcontrol.server.modules.auth.domain.model.OtpPurpose
import java.time.Instant

sealed interface OtpConsumeResult {
    data class Success(val userId: Long?) : OtpConsumeResult
    data object Invalid : OtpConsumeResult
    data object Expired : OtpConsumeResult
}

interface OtpRepository {
    suspend fun createOtp(
        email: String,
        userId: Long?,
        purpose: OtpPurpose,
        codeHash: String,
        expiresAt: Instant,
    )

    suspend fun consumeOtp(
        email: String,
        purpose: OtpPurpose,
        codeHash: String,
        now: Instant,
    ): OtpConsumeResult
}


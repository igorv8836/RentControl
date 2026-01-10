package org.igorv8836.rentcontrol.server.modules.auth.domain.service

import io.ktor.http.HttpStatusCode
import org.igorv8836.rentcontrol.server.foundation.errors.ApiErrorDetail
import org.igorv8836.rentcontrol.server.foundation.errors.ApiException
import org.igorv8836.rentcontrol.server.foundation.security.UserRole
import org.igorv8836.rentcontrol.server.foundation.security.UserStatus
import org.igorv8836.rentcontrol.server.modules.auth.domain.model.IssuedTokenPair
import org.igorv8836.rentcontrol.server.modules.auth.domain.model.OtpPurpose
import org.igorv8836.rentcontrol.server.modules.auth.domain.port.AuthSessionRepository
import org.igorv8836.rentcontrol.server.modules.auth.domain.port.OtpConsumeResult
import org.igorv8836.rentcontrol.server.modules.auth.domain.port.OtpRepository
import org.igorv8836.rentcontrol.server.modules.auth.domain.port.OtpSender
import org.igorv8836.rentcontrol.server.modules.users.domain.port.UsersRepository
import java.time.Duration
import java.time.Instant
import kotlin.random.Random

class AuthService(
    private val usersRepository: UsersRepository,
    private val sessionsRepository: AuthSessionRepository,
    private val otpRepository: OtpRepository,
    private val passwordHasher: PasswordHasher,
    private val tokenService: TokenService,
    private val otpSender: OtpSender,
) {
    suspend fun register(email: String, password: String) {
        val normalizedEmail = validateEmail(email)
        validatePassword(password)

        val existing = usersRepository.findByEmail(normalizedEmail)
        val user = when (existing?.status) {
            null -> usersRepository.createUser(
                email = normalizedEmail,
                passwordHash = passwordHasher.hash(password),
                role = UserRole.TENANT,
                status = UserStatus.PENDING,
            )

            UserStatus.PENDING -> {
                usersRepository.updatePassword(existing.id, passwordHasher.hash(password))
                existing
            }

            UserStatus.ACTIVE -> throw ApiException(
                status = HttpStatusCode.Conflict,
                code = "email_exists",
                message = "Email already registered",
            )

            UserStatus.BLOCKED -> throw ApiException(
                status = HttpStatusCode.Forbidden,
                code = "account_blocked",
                message = "Account is blocked",
            )
        }

        val code = generateOtpCode()
        otpRepository.createOtp(
            email = normalizedEmail,
            userId = user.id,
            purpose = OtpPurpose.REGISTER,
            codeHash = tokenService.hash(code),
            expiresAt = Instant.now().plus(OTP_TTL),
        )
        otpSender.send(normalizedEmail, code)
    }

    suspend fun confirm(email: String, otp: String): IssuedTokenPair {
        val normalizedEmail = validateEmail(email)
        val user = usersRepository.findByEmail(normalizedEmail)
            ?: throw ApiException(
                status = HttpStatusCode.NotFound,
                code = "not_found",
                message = "User not found",
            )

        if (user.status == UserStatus.BLOCKED) {
            throw ApiException(
                status = HttpStatusCode.Forbidden,
                code = "account_blocked",
                message = "Account is blocked",
            )
        }

        val consumeResult = otpRepository.consumeOtp(
            email = normalizedEmail,
            purpose = OtpPurpose.REGISTER,
            codeHash = tokenService.hash(otp),
            now = Instant.now(),
        )

        when (consumeResult) {
            is OtpConsumeResult.Success -> {
                usersRepository.updateStatus(user.id, UserStatus.ACTIVE)
            }

            OtpConsumeResult.Invalid -> throw ApiException(
                status = HttpStatusCode.BadRequest,
                code = "otp_invalid",
                message = "Invalid OTP code",
            )

            OtpConsumeResult.Expired -> throw ApiException(
                status = HttpStatusCode.BadRequest,
                code = "otp_expired",
                message = "OTP code expired",
            )
        }

        val issued = tokenService.issueTokenPair()
        sessionsRepository.createSession(user.id, issued)
        return issued
    }

    suspend fun login(email: String, password: String): IssuedTokenPair {
        val normalizedEmail = validateEmail(email)
        val user = usersRepository.findByEmail(normalizedEmail)
            ?: throw invalidCredentials()

        if (!passwordHasher.verify(password, user.passwordHash)) {
            throw invalidCredentials()
        }

        when (user.status) {
            UserStatus.PENDING -> throw ApiException(
                status = HttpStatusCode.Forbidden,
                code = "account_not_confirmed",
                message = "Account is not confirmed",
            )

            UserStatus.BLOCKED -> throw ApiException(
                status = HttpStatusCode.Forbidden,
                code = "account_blocked",
                message = "Account is blocked",
            )

            UserStatus.ACTIVE -> Unit
        }

        val issued = tokenService.issueTokenPair()
        sessionsRepository.createSession(user.id, issued)
        return issued
    }

    suspend fun refresh(refreshToken: String): IssuedTokenPair {
        val hash = tokenService.hash(refreshToken)
        val issued = tokenService.issueTokenPair()
        sessionsRepository.rotateSession(hash, issued)
            ?: throw ApiException(
                status = HttpStatusCode.Unauthorized,
                code = "unauthorized",
                message = "Invalid refresh token",
            )
        return issued
    }

    suspend fun logout(refreshToken: String) {
        val hash = tokenService.hash(refreshToken)
        val revoked = sessionsRepository.revokeByRefreshTokenHash(hash)
        if (!revoked) {
            throw ApiException(
                status = HttpStatusCode.Unauthorized,
                code = "unauthorized",
                message = "Invalid refresh token",
            )
        }
    }

    suspend fun requestPasswordReset(email: String) {
        val normalizedEmail = validateEmail(email)
        val user = usersRepository.findByEmail(normalizedEmail)
            ?: return

        if (user.status == UserStatus.BLOCKED) {
            return
        }

        val code = generateOtpCode()
        otpRepository.createOtp(
            email = normalizedEmail,
            userId = user.id,
            purpose = OtpPurpose.PASSWORD_RESET,
            codeHash = tokenService.hash(code),
            expiresAt = Instant.now().plus(OTP_TTL),
        )
        otpSender.send(normalizedEmail, code)
    }

    suspend fun confirmPasswordReset(email: String, otp: String, newPassword: String) {
        val normalizedEmail = validateEmail(email)
        validatePassword(newPassword)

        val user = usersRepository.findByEmail(normalizedEmail)
            ?: throw ApiException(
                status = HttpStatusCode.BadRequest,
                code = "otp_invalid",
                message = "Invalid OTP code",
            )

        if (user.status == UserStatus.BLOCKED) {
            throw ApiException(
                status = HttpStatusCode.Forbidden,
                code = "account_blocked",
                message = "Account is blocked",
            )
        }

        val consumeResult = otpRepository.consumeOtp(
            email = normalizedEmail,
            purpose = OtpPurpose.PASSWORD_RESET,
            codeHash = tokenService.hash(otp),
            now = Instant.now(),
        )

        when (consumeResult) {
            is OtpConsumeResult.Success -> Unit
            OtpConsumeResult.Invalid -> throw ApiException(
                status = HttpStatusCode.BadRequest,
                code = "otp_invalid",
                message = "Invalid OTP code",
            )

            OtpConsumeResult.Expired -> throw ApiException(
                status = HttpStatusCode.BadRequest,
                code = "otp_expired",
                message = "OTP code expired",
            )
        }

        usersRepository.updatePassword(user.id, passwordHasher.hash(newPassword))
        sessionsRepository.revokeAllForUser(user.id)
    }

    private fun validateEmail(email: String): String {
        val normalized = email.trim().lowercase()
        if (normalized.isBlank() || !EMAIL_REGEX.matches(normalized)) {
            throw ApiException(
                status = HttpStatusCode.BadRequest,
                code = "validation_error",
                message = "Invalid email",
                details = listOf(ApiErrorDetail(field = "email", issue = "invalid")),
            )
        }
        return normalized
    }

    private fun validatePassword(password: String) {
        if (password.length < MIN_PASSWORD_LENGTH) {
            throw ApiException(
                status = HttpStatusCode.BadRequest,
                code = "validation_error",
                message = "Invalid password",
                details = listOf(ApiErrorDetail(field = "password", issue = "too_short")),
            )
        }
    }

    private fun invalidCredentials(): ApiException = ApiException(
        status = HttpStatusCode.BadRequest,
        code = "invalid_credentials",
        message = "Invalid email or password",
    )

    private fun generateOtpCode(): String {
        val value = Random.nextInt(0, 1_000_000)
        return value.toString().padStart(6, '0')
    }

    private companion object {
        private val EMAIL_REGEX = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
        private const val MIN_PASSWORD_LENGTH = 8
        private val OTP_TTL: Duration = Duration.ofMinutes(10)
    }
}

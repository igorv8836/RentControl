package org.igorv8836.rentcontrol.server.modules.auth.domain.model

enum class OtpPurpose {
    REGISTER,
    PASSWORD_RESET,
}

fun OtpPurpose.toDbValue(): String = when (this) {
    OtpPurpose.REGISTER -> "register"
    OtpPurpose.PASSWORD_RESET -> "password_reset"
}

fun otpPurposeFromDb(value: String): OtpPurpose = when (value) {
    "register" -> OtpPurpose.REGISTER
    "password_reset" -> OtpPurpose.PASSWORD_RESET
    else -> error("Unknown OTP purpose: $value")
}


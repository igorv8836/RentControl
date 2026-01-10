package org.igorv8836.rentcontrol.server.integrations.otp

import io.ktor.util.logging.Logger
import org.igorv8836.rentcontrol.server.modules.auth.domain.port.OtpSender

class MockOtpSender(
    private val logger: Logger,
) : OtpSender {
    override fun send(email: String, code: String) {
        logger.info("Mock OTP for email=$email: $code")
    }
}


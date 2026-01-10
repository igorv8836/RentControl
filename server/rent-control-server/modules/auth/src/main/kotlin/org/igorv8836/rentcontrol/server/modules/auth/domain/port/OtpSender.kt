package org.igorv8836.rentcontrol.server.modules.auth.domain.port

interface OtpSender {
    fun send(email: String, code: String)
}


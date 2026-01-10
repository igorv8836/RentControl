package org.igorv8836.rentcontrol.server.modules.auth.domain.model

import java.time.Instant

data class IssuedToken(
    val token: String,
    val hash: String,
    val expiresAt: Instant,
)

data class IssuedTokenPair(
    val access: IssuedToken,
    val refresh: IssuedToken,
)


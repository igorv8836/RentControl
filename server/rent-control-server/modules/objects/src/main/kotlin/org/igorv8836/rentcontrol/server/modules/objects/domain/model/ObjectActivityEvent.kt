package org.igorv8836.rentcontrol.server.modules.objects.domain.model

import java.time.Instant

data class ObjectActivityEvent(
    val id: Long,
    val createdAt: Instant,
    val actor: ObjectActivityActor?,
    val action: String,
)

data class ObjectActivityActor(
    val id: Long,
    val fullName: String,
    val email: String,
    val phone: String?,
)


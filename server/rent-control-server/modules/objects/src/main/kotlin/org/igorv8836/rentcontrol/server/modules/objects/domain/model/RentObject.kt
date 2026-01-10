package org.igorv8836.rentcontrol.server.modules.objects.domain.model

import java.time.Instant

data class RentObject(
    val id: Long,
    val address: String,
    val type: String,
    val area: Double?,
    val status: ObjectOccupancyStatus,
    val notes: String?,
    val ownerId: Long,
    val tenantId: Long?,
    val archivedAt: Instant?,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    val isArchived: Boolean get() = archivedAt != null
}


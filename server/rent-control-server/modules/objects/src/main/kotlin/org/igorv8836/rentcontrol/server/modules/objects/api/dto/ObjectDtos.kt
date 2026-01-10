package org.igorv8836.rentcontrol.server.modules.objects.api.dto

import kotlinx.serialization.Serializable
import org.igorv8836.rentcontrol.server.modules.objects.domain.model.ObjectOccupancyStatus

@Serializable
data class ObjectsListResponse(
    val page: Int,
    val pageSize: Int,
    val total: Long,
    val items: List<ObjectListItem>,
)

@Serializable
data class ObjectListItem(
    val id: Long,
    val address: String,
    val type: String,
    val area: Double? = null,
    val status: ObjectOccupancyStatus,
    val isArchived: Boolean,
    val tenant: ObjectUserSummary? = null,
)

@Serializable
data class ObjectDetailsResponse(
    val id: Long,
    val address: String,
    val type: String,
    val area: Double? = null,
    val status: ObjectOccupancyStatus,
    val notes: String? = null,
    val ownerId: Long,
    val tenant: ObjectUserSummary? = null,
    val isArchived: Boolean,
)

@Serializable
data class CreateObjectRequest(
    val address: String,
    val type: String,
    val area: Double? = null,
    val notes: String? = null,
    val tenantId: Long? = null,
    val ownerId: Long? = null,
)

@Serializable
data class UpdateObjectRequest(
    val address: String? = null,
    val type: String? = null,
    val area: Double? = null,
    val notes: String? = null,
    val tenantId: Long? = null,
    val status: ObjectOccupancyStatus? = null,
)

@Serializable
data class ObjectUserSummary(
    val id: Long,
    val fullName: String,
    val email: String,
    val phone: String? = null,
)

@Serializable
data class ObjectActivityResponse(
    val items: List<ObjectActivityItem>,
)

@Serializable
data class ObjectActivityItem(
    val type: String,
    val title: String,
    val body: String? = null,
)


package org.igorv8836.rentcontrol.server.modules.objects.domain.port

import org.igorv8836.rentcontrol.server.foundation.security.UserContext
import org.igorv8836.rentcontrol.server.modules.objects.domain.model.ObjectOccupancyStatus
import org.igorv8836.rentcontrol.server.modules.objects.domain.model.RentObject

data class ObjectsListQuery(
    val search: String? = null,
    val status: ObjectOccupancyStatus? = null,
    val includeArchived: Boolean = false,
    val page: Int = 1,
    val pageSize: Int = 20,
)

data class ObjectsPage(
    val page: Int,
    val pageSize: Int,
    val total: Long,
    val items: List<RentObject>,
)

data class CreateObjectData(
    val address: String,
    val type: String,
    val area: Double?,
    val notes: String?,
    val status: ObjectOccupancyStatus,
    val ownerId: Long,
    val tenantId: Long?,
)

data class UpdateObjectPatch(
    val address: String? = null,
    val type: String? = null,
    val area: Double? = null,
    val notes: String? = null,
    val status: ObjectOccupancyStatus? = null,
    val tenantId: Long? = null,
)

interface ObjectsRepository {
    suspend fun listForUser(user: UserContext, query: ObjectsListQuery): ObjectsPage
    suspend fun getForUser(user: UserContext, objectId: Long): RentObject?
    suspend fun create(data: CreateObjectData): RentObject
    suspend fun updateForUser(user: UserContext, objectId: Long, patch: UpdateObjectPatch): RentObject?
    suspend fun setArchivedForUser(user: UserContext, objectId: Long, archived: Boolean): RentObject?
}


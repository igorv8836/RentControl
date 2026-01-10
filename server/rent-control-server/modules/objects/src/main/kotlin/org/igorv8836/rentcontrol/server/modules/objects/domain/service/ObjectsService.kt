package org.igorv8836.rentcontrol.server.modules.objects.domain.service

import io.ktor.http.HttpStatusCode
import org.igorv8836.rentcontrol.server.foundation.errors.ApiException
import org.igorv8836.rentcontrol.server.foundation.security.UserContext
import org.igorv8836.rentcontrol.server.foundation.security.UserRole
import org.igorv8836.rentcontrol.server.modules.objects.domain.model.ObjectOccupancyStatus
import org.igorv8836.rentcontrol.server.modules.objects.domain.model.RentObject
import org.igorv8836.rentcontrol.server.modules.objects.domain.port.CreateObjectData
import org.igorv8836.rentcontrol.server.modules.objects.domain.port.ObjectsListQuery
import org.igorv8836.rentcontrol.server.modules.objects.domain.port.ObjectsPage
import org.igorv8836.rentcontrol.server.modules.objects.domain.port.ObjectsRepository
import org.igorv8836.rentcontrol.server.modules.objects.domain.port.UpdateObjectPatch
import org.igorv8836.rentcontrol.server.modules.users.domain.model.User
import org.igorv8836.rentcontrol.server.modules.users.domain.port.UsersRepository

class ObjectsService(
    private val objectsRepository: ObjectsRepository,
    private val usersRepository: UsersRepository,
) {
    suspend fun listObjects(user: UserContext, query: ObjectsListQuery): ObjectsPage {
        requireValidPaging(query.page, query.pageSize)
        return objectsRepository.listForUser(user, query)
    }

    suspend fun getObject(user: UserContext, objectId: Long): Pair<RentObject, User?> {
        val obj = objectsRepository.getForUser(user, objectId)
            ?: throw ApiException(
                status = HttpStatusCode.NotFound,
                code = "not_found",
                message = "Object not found",
            )

        val tenant = obj.tenantId?.let { usersRepository.getById(it) }
        return obj to tenant
    }

    suspend fun createObject(
        user: UserContext,
        address: String,
        type: String,
        area: Double?,
        notes: String?,
        tenantId: Long?,
        ownerId: Long?,
    ): Pair<RentObject, User?> {
        requireWriteRole(user)

        val normalizedAddress = address.trim()
        if (normalizedAddress.isBlank()) {
            throw ApiException(
                status = HttpStatusCode.BadRequest,
                code = "invalid_argument",
                message = "Address is required",
            )
        }

        val normalizedType = type.trim()
        if (normalizedType.isBlank()) {
            throw ApiException(
                status = HttpStatusCode.BadRequest,
                code = "invalid_argument",
                message = "Type is required",
            )
        }

        if (area != null && area <= 0.0) {
            throw ApiException(
                status = HttpStatusCode.BadRequest,
                code = "invalid_argument",
                message = "Area must be greater than zero",
            )
        }

        val resolvedOwnerId = when (user.role) {
            UserRole.ADMIN -> ownerId ?: user.userId
            UserRole.LANDLORD -> {
                if (ownerId != null && ownerId != user.userId) {
                    throw ApiException(
                        status = HttpStatusCode.Forbidden,
                        code = "forbidden",
                        message = "Cannot create object for another owner",
                    )
                }
                user.userId
            }
            else -> user.userId
        }

        val tenant = tenantId?.let { validateTenant(it) }
        val status = if (tenantId != null) ObjectOccupancyStatus.LEASED else ObjectOccupancyStatus.AVAILABLE

        val created = objectsRepository.create(
            CreateObjectData(
                address = normalizedAddress,
                type = normalizedType,
                area = area,
                notes = notes?.trim()?.takeIf { it.isNotBlank() },
                status = status,
                ownerId = resolvedOwnerId,
                tenantId = tenantId,
            ),
        )

        return created to tenant
    }

    suspend fun updateObject(
        user: UserContext,
        objectId: Long,
        patch: UpdateObjectPatch,
    ): Pair<RentObject, User?> {
        requireWriteRole(user)

        if (patch.address != null && patch.address.isBlank()) {
            throw ApiException(
                status = HttpStatusCode.BadRequest,
                code = "invalid_argument",
                message = "Address cannot be blank",
            )
        }

        if (patch.type != null && patch.type.isBlank()) {
            throw ApiException(
                status = HttpStatusCode.BadRequest,
                code = "invalid_argument",
                message = "Type cannot be blank",
            )
        }

        if (patch.area != null && patch.area <= 0.0) {
            throw ApiException(
                status = HttpStatusCode.BadRequest,
                code = "invalid_argument",
                message = "Area must be greater than zero",
            )
        }

        val tenant = patch.tenantId?.let { validateTenant(it) }

        val normalizedPatch = patch.copy(
            address = patch.address?.trim(),
            type = patch.type?.trim(),
            notes = patch.notes?.trim()?.takeIf { it.isNotBlank() },
        )

        val updated = objectsRepository.updateForUser(user, objectId, normalizedPatch)
            ?: throw ApiException(
                status = HttpStatusCode.NotFound,
                code = "not_found",
                message = "Object not found",
            )

        return updated to tenant
    }

    suspend fun archiveObject(user: UserContext, objectId: Long): RentObject {
        requireWriteRole(user)
        return objectsRepository.setArchivedForUser(user, objectId, archived = true)
            ?: throw ApiException(
                status = HttpStatusCode.NotFound,
                code = "not_found",
                message = "Object not found",
            )
    }

    suspend fun unarchiveObject(user: UserContext, objectId: Long): RentObject {
        requireWriteRole(user)
        return objectsRepository.setArchivedForUser(user, objectId, archived = false)
            ?: throw ApiException(
                status = HttpStatusCode.NotFound,
                code = "not_found",
                message = "Object not found",
            )
    }

    private suspend fun validateTenant(tenantId: Long): User {
        val tenant = usersRepository.getById(tenantId)
            ?: throw ApiException(
                status = HttpStatusCode.BadRequest,
                code = "invalid_argument",
                message = "Unknown tenantId",
            )
        if (tenant.role != UserRole.TENANT) {
            throw ApiException(
                status = HttpStatusCode.BadRequest,
                code = "invalid_argument",
                message = "tenantId must reference a tenant user",
            )
        }
        return tenant
    }

    private fun requireWriteRole(user: UserContext) {
        if (user.role != UserRole.ADMIN && user.role != UserRole.LANDLORD) {
            throw ApiException(
                status = HttpStatusCode.Forbidden,
                code = "forbidden",
                message = "Insufficient permissions",
            )
        }
    }

    private fun requireValidPaging(page: Int, pageSize: Int) {
        if (page < 1) {
            throw ApiException(
                status = HttpStatusCode.BadRequest,
                code = "invalid_argument",
                message = "page must be >= 1",
            )
        }
        if (pageSize !in 1..100) {
            throw ApiException(
                status = HttpStatusCode.BadRequest,
                code = "invalid_argument",
                message = "pageSize must be in 1..100",
            )
        }
    }
}

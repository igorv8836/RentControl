package org.igorv8836.rentcontrol.server.modules.objects.module

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.igorv8836.rentcontrol.server.foundation.errors.ApiException
import org.igorv8836.rentcontrol.server.foundation.security.userContext
import org.igorv8836.rentcontrol.server.modules.objects.api.dto.CreateObjectRequest
import org.igorv8836.rentcontrol.server.modules.objects.api.dto.ObjectActivityResponse
import org.igorv8836.rentcontrol.server.modules.objects.api.dto.ObjectDefectsOverviewResponse
import org.igorv8836.rentcontrol.server.modules.objects.api.dto.ObjectDetailsResponse
import org.igorv8836.rentcontrol.server.modules.objects.api.dto.ObjectInspectionsOverviewResponse
import org.igorv8836.rentcontrol.server.modules.objects.api.dto.ObjectListItem
import org.igorv8836.rentcontrol.server.modules.objects.api.dto.ObjectMetersOverviewResponse
import org.igorv8836.rentcontrol.server.modules.objects.api.dto.ObjectOverviewResponse
import org.igorv8836.rentcontrol.server.modules.objects.api.dto.ObjectUserSummary
import org.igorv8836.rentcontrol.server.modules.objects.api.dto.ObjectsListResponse
import org.igorv8836.rentcontrol.server.modules.objects.api.dto.UpdateObjectRequest
import org.igorv8836.rentcontrol.server.modules.objects.domain.model.ObjectAggregates
import org.igorv8836.rentcontrol.server.modules.objects.domain.model.ObjectOccupancyStatus
import org.igorv8836.rentcontrol.server.modules.objects.domain.model.RentObject
import org.igorv8836.rentcontrol.server.modules.objects.domain.port.ObjectsListQuery
import org.igorv8836.rentcontrol.server.modules.objects.domain.port.UpdateObjectPatch
import org.igorv8836.rentcontrol.server.modules.objects.domain.service.ObjectDetails
import org.igorv8836.rentcontrol.server.modules.objects.domain.service.ObjectsService
import org.igorv8836.rentcontrol.server.modules.users.domain.model.User
import java.time.Instant

fun Route.objectsModule(objectsService: ObjectsService) {
    route("/objects") {
        get {
            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
            val pageSize = call.request.queryParameters["pageSize"]?.toIntOrNull() ?: 20
            val search = call.request.queryParameters["q"]?.trim()?.takeIf { it.isNotBlank() }
            val includeArchived = call.request.queryParameters["includeArchived"]?.toBooleanStrictOrNull() ?: false

            val status = call.request.queryParameters["status"]
                ?.trim()
                ?.takeIf { it.isNotBlank() }
                ?.let(::parseStatus)

            val result = objectsService.listObjects(
                user = call.userContext,
                query = ObjectsListQuery(
                    search = search,
                    status = status,
                    includeArchived = includeArchived,
                    page = page,
                    pageSize = pageSize,
                ),
            )

            call.respond(
                ObjectsListResponse(
                    page = result.page,
                    pageSize = result.pageSize,
                    total = result.total,
                    items = result.items.map { it.toListItem(tenant = null) },
                ),
            )
        }

        post {
            val request = call.receive<CreateObjectRequest>()
            val (obj, tenant) = objectsService.createObject(
                user = call.userContext,
                address = request.address,
                type = request.type,
                area = request.area,
                notes = request.notes,
                tenantId = request.tenantId,
                ownerId = request.ownerId,
            )
            call.respond(HttpStatusCode.Created, obj.toDetailsResponse(tenant))
        }

        route("/{objectId}") {
            get {
                val objectId = call.objectIdOrThrow()
                val details = objectsService.getObjectDetails(call.userContext, objectId)
                call.respond(details.toDetailsResponse())
            }

            patch {
                val objectId = call.objectIdOrThrow()
                val request = call.receive<UpdateObjectRequest>()
                val (obj, tenant) = objectsService.updateObject(
                    user = call.userContext,
                    objectId = objectId,
                    patch = UpdateObjectPatch(
                        address = request.address,
                        type = request.type,
                        area = request.area,
                        notes = request.notes,
                        status = request.status,
                        tenantId = request.tenantId,
                    ),
                )
                call.respond(obj.toDetailsResponse(tenant))
            }

            post("/archive") {
                val objectId = call.objectIdOrThrow()
                val obj = objectsService.archiveObject(call.userContext, objectId)
                call.respond(obj.toDetailsResponse(tenant = null))
            }

            post("/unarchive") {
                val objectId = call.objectIdOrThrow()
                val obj = objectsService.unarchiveObject(call.userContext, objectId)
                call.respond(obj.toDetailsResponse(tenant = null))
            }

            get("/activity") {
                call.objectIdOrThrow()
                call.respond(ObjectActivityResponse(items = emptyList()))
            }
        }
    }
}

private fun parseStatus(value: String): ObjectOccupancyStatus {
    return runCatching { ObjectOccupancyStatus.valueOf(value.uppercase()) }
        .getOrNull()
        ?: throw ApiException(
            status = HttpStatusCode.BadRequest,
            code = "invalid_argument",
            message = "Invalid status",
        )
}

private fun io.ktor.server.application.ApplicationCall.objectIdOrThrow(): Long =
    parameters["objectId"]?.toLongOrNull()
        ?: throw ApiException(
            status = HttpStatusCode.BadRequest,
            code = "invalid_argument",
            message = "Invalid objectId",
        )

private fun RentObject.toListItem(tenant: User?): ObjectListItem =
    ObjectListItem(
        id = id,
        address = address,
        type = type,
        area = area,
        status = status,
        isArchived = isArchived,
        tenant = tenant?.toUserSummary(),
    )

private fun RentObject.toDetailsResponse(tenant: User?): ObjectDetailsResponse =
    ObjectDetailsResponse(
        id = id,
        address = address,
        type = type,
        area = area,
        status = status,
        notes = notes,
        ownerId = ownerId,
        tenant = tenant?.toUserSummary(),
        isArchived = isArchived,
    )

private fun ObjectDetails.toDetailsResponse(): ObjectDetailsResponse =
    obj.toDetailsResponse(
        tenant = tenant,
        aggregates = aggregates,
    )

private fun RentObject.toDetailsResponse(
    tenant: User?,
    aggregates: ObjectAggregates?,
): ObjectDetailsResponse =
    ObjectDetailsResponse(
        id = id,
        address = address,
        type = type,
        area = area,
        status = status,
        notes = notes,
        ownerId = ownerId,
        tenant = tenant?.toUserSummary(),
        isArchived = isArchived,
        overview = aggregates?.toOverviewResponse(),
    )

private fun ObjectAggregates.toOverviewResponse(): ObjectOverviewResponse =
    ObjectOverviewResponse(
        defects = ObjectDefectsOverviewResponse(
            openCount = defectsOpenCount,
            overdueCount = defectsOverdueCount,
        ),
        inspections = ObjectInspectionsOverviewResponse(
            nextScheduledAt = nextInspectionAt?.toIsoString(),
            lastCompletedAt = lastInspectionAt?.toIsoString(),
        ),
        meters = ObjectMetersOverviewResponse(
            lastReadingAt = lastMeterReadingAt?.toIsoString(),
        ),
    )

private fun Instant.toIsoString(): String = toString()

private fun User.toUserSummary(): ObjectUserSummary =
    ObjectUserSummary(
        id = id,
        fullName = fullName,
        email = email,
        phone = phone,
    )

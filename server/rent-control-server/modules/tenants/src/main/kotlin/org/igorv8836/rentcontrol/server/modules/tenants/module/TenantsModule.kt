package org.igorv8836.rentcontrol.server.modules.tenants.module

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
import org.igorv8836.rentcontrol.server.modules.tenants.api.dto.CreateTenantRequest
import org.igorv8836.rentcontrol.server.modules.tenants.api.dto.TenantDetailsResponse
import org.igorv8836.rentcontrol.server.modules.tenants.api.dto.TenantListItem
import org.igorv8836.rentcontrol.server.modules.tenants.api.dto.TenantsListResponse
import org.igorv8836.rentcontrol.server.modules.tenants.api.dto.UpdateTenantRequest
import org.igorv8836.rentcontrol.server.modules.tenants.domain.service.TenantsListQuery
import org.igorv8836.rentcontrol.server.modules.tenants.domain.service.TenantsService
import org.igorv8836.rentcontrol.server.modules.users.domain.model.User

fun Route.tenantsModule(tenantsService: TenantsService) {
    route("/tenants") {
        get {
            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
            val pageSize = call.request.queryParameters["pageSize"]?.toIntOrNull() ?: 20
            val search = call.request.queryParameters["q"]?.trim()?.takeIf { it.isNotBlank() }

            val result = tenantsService.listTenants(
                user = call.userContext,
                query = TenantsListQuery(
                    search = search,
                    page = page,
                    pageSize = pageSize,
                ),
            )

            call.respond(
                TenantsListResponse(
                    page = result.page,
                    pageSize = result.pageSize,
                    total = result.total,
                    items = result.items.map { it.toListItem() },
                ),
            )
        }

        post {
            val request = call.receive<CreateTenantRequest>()
            val created = tenantsService.createTenant(
                user = call.userContext,
                email = request.email,
                fullName = request.fullName,
                phone = request.phone,
            )
            call.respond(HttpStatusCode.Created, created.toDetailsResponse())
        }

        route("/{tenantId}") {
            get {
                val tenantId = call.tenantIdOrThrow()
                val tenant = tenantsService.getTenant(call.userContext, tenantId)
                call.respond(tenant.toDetailsResponse())
            }

            patch {
                val tenantId = call.tenantIdOrThrow()
                val request = call.receive<UpdateTenantRequest>()
                val updated = tenantsService.updateTenant(
                    user = call.userContext,
                    tenantId = tenantId,
                    fullName = request.fullName,
                    phone = request.phone,
                )
                call.respond(updated.toDetailsResponse())
            }
        }
    }
}

private fun io.ktor.server.application.ApplicationCall.tenantIdOrThrow(): Long =
    parameters["tenantId"]?.toLongOrNull()
        ?: throw ApiException(
            status = HttpStatusCode.BadRequest,
            code = "invalid_argument",
            message = "Invalid tenantId",
        )

private fun User.toListItem(): TenantListItem =
    TenantListItem(
        id = id,
        fullName = fullName,
        email = email,
        phone = phone,
        status = status,
    )

private fun User.toDetailsResponse(): TenantDetailsResponse =
    TenantDetailsResponse(
        id = id,
        fullName = fullName,
        email = email,
        phone = phone,
        status = status,
    )

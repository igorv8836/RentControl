package org.igorv8836.rentcontrol.server.modules.tenants.api.dto

import kotlinx.serialization.Serializable
import org.igorv8836.rentcontrol.server.foundation.security.UserStatus

@Serializable
data class TenantsListResponse(
    val page: Int,
    val pageSize: Int,
    val total: Long,
    val items: List<TenantListItem>,
)

@Serializable
data class TenantListItem(
    val id: Long,
    val fullName: String,
    val email: String,
    val phone: String? = null,
    val status: UserStatus,
)

@Serializable
data class CreateTenantRequest(
    val email: String,
    val fullName: String,
    val phone: String? = null,
)

@Serializable
data class UpdateTenantRequest(
    val fullName: String? = null,
    val phone: String? = null,
)

@Serializable
data class TenantDetailsResponse(
    val id: Long,
    val fullName: String,
    val email: String,
    val phone: String? = null,
    val status: UserStatus,
)

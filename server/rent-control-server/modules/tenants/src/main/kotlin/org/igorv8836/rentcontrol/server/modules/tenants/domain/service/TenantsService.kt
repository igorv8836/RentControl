package org.igorv8836.rentcontrol.server.modules.tenants.domain.service

import io.ktor.http.HttpStatusCode
import org.igorv8836.rentcontrol.server.foundation.errors.ApiErrorDetail
import org.igorv8836.rentcontrol.server.foundation.errors.ApiException
import org.igorv8836.rentcontrol.server.foundation.security.UserContext
import org.igorv8836.rentcontrol.server.foundation.security.UserRole
import org.igorv8836.rentcontrol.server.foundation.security.UserStatus
import org.igorv8836.rentcontrol.server.modules.users.domain.model.User
import org.igorv8836.rentcontrol.server.modules.users.domain.port.UsersListQuery
import org.igorv8836.rentcontrol.server.modules.users.domain.port.UsersPage
import org.igorv8836.rentcontrol.server.modules.users.domain.port.UsersRepository

class TenantsService(
    private val usersRepository: UsersRepository,
) {
    suspend fun listTenants(user: UserContext, query: TenantsListQuery): UsersPage {
        requireManageTenantsRole(user)
        requireValidPaging(query.page, query.pageSize)

        return usersRepository.listUsers(
            UsersListQuery(
                search = query.search,
                role = UserRole.TENANT,
                page = query.page,
                pageSize = query.pageSize,
            ),
        )
    }

    suspend fun createTenant(
        user: UserContext,
        email: String,
        fullName: String,
        phone: String?,
    ): User {
        requireManageTenantsRole(user)

        val normalizedEmail = validateEmail(email)
        val normalizedFullName = fullName.trim()
        if (normalizedFullName.isBlank()) {
            throw ApiException(
                status = HttpStatusCode.BadRequest,
                code = "validation_error",
                message = "Invalid fullName",
                details = listOf(ApiErrorDetail(field = "fullName", issue = "blank")),
            )
        }

        usersRepository.findByEmail(normalizedEmail)?.let {
            throw ApiException(
                status = HttpStatusCode.Conflict,
                code = "email_exists",
                message = "Email already registered",
            )
        }

        val created = usersRepository.createUser(
            email = normalizedEmail,
            passwordHash = "unset",
            role = UserRole.TENANT,
            status = UserStatus.ACTIVE,
        )

        return usersRepository.updateUser(
            userId = created.id,
            fullName = normalizedFullName,
            phone = phone?.trim()?.takeIf { it.isNotBlank() },
            preferences = null,
        )
    }

    suspend fun getTenant(user: UserContext, tenantId: Long): User {
        requireManageTenantsRole(user)

        val tenant = usersRepository.getById(tenantId)
            ?: throw ApiException(
                status = HttpStatusCode.NotFound,
                code = "not_found",
                message = "Tenant not found",
            )

        if (tenant.role != UserRole.TENANT) {
            throw ApiException(
                status = HttpStatusCode.NotFound,
                code = "not_found",
                message = "Tenant not found",
            )
        }

        return tenant
    }

    suspend fun updateTenant(
        user: UserContext,
        tenantId: Long,
        fullName: String?,
        phone: String?,
    ): User {
        requireManageTenantsRole(user)

        getTenant(user, tenantId)

        if (fullName != null && fullName.isBlank()) {
            throw ApiException(
                status = HttpStatusCode.BadRequest,
                code = "validation_error",
                message = "Invalid fullName",
                details = listOf(ApiErrorDetail(field = "fullName", issue = "blank")),
            )
        }

        return usersRepository.updateUser(
            userId = tenantId,
            fullName = fullName?.trim(),
            phone = phone?.trim(),
            preferences = null,
        )
    }

    private fun requireManageTenantsRole(user: UserContext) {
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

    private fun validateEmail(email: String): String {
        val normalized = email.trim().lowercase()
        if (normalized.isBlank() || !EMAIL_REGEX.matches(normalized)) {
            throw ApiException(
                status = HttpStatusCode.BadRequest,
                code = "validation_error",
                message = "Invalid email",
                details = listOf(ApiErrorDetail(field = "email", issue = "invalid")),
            )
        }
        return normalized
    }

    private companion object {
        private val EMAIL_REGEX = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
    }
}

data class TenantsListQuery(
    val search: String? = null,
    val page: Int = 1,
    val pageSize: Int = 20,
)

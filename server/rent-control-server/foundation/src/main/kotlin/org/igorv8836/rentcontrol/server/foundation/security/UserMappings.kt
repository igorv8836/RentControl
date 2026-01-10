package org.igorv8836.rentcontrol.server.foundation.security

fun UserRole.toDbValue(): String = when (this) {
    UserRole.ADMIN -> "admin"
    UserRole.LANDLORD -> "landlord"
    UserRole.INSPECTOR -> "inspector"
    UserRole.TENANT -> "tenant"
}

fun userRoleFromDb(value: String): UserRole = when (value) {
    "admin" -> UserRole.ADMIN
    "landlord" -> UserRole.LANDLORD
    "inspector" -> UserRole.INSPECTOR
    "tenant" -> UserRole.TENANT
    else -> error("Unknown role: $value")
}

fun UserStatus.toDbValue(): String = when (this) {
    UserStatus.PENDING -> "pending"
    UserStatus.ACTIVE -> "active"
    UserStatus.BLOCKED -> "blocked"
}

fun userStatusFromDb(value: String): UserStatus = when (value) {
    "pending" -> UserStatus.PENDING
    "active" -> UserStatus.ACTIVE
    "blocked" -> UserStatus.BLOCKED
    else -> error("Unknown status: $value")
}


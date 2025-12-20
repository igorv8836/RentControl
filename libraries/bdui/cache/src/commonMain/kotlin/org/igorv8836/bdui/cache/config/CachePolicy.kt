package org.igorv8836.bdui.cache.config

enum class CacheKind {
    Memory,
    Disk,
}

data class CachePolicy(
    val enabled: Boolean = false,
    val kind: CacheKind = CacheKind.Memory,
    val ttlMillis: Long? = null,
)

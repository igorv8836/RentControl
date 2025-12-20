package org.igorv8836.bdui.cache.store

/**
 * Minimal file storage abstraction for cache persistence.
 * Implementations must ensure directory exists and operate with UTF-8 text.
 */
expect class DiskStorage(basePath: String) {
    suspend fun write(key: String, content: String)
    suspend fun read(key: String): String?
    suspend fun delete(key: String)
    suspend fun listKeys(): List<String>
}

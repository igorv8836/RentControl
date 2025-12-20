package org.igorv8836.bdui.cache.store

import org.igorv8836.bdui.contract.Screen

data class CachedScreenEntry(
    val screen: Screen,
    val expiresAtMillis: Long?,
)

interface ScreenCacheStore {
    suspend fun get(key: String): CachedScreenEntry?
    suspend fun put(key: String, entry: CachedScreenEntry)
    suspend fun remove(key: String)
    suspend fun clearExpired(now: Long)
}

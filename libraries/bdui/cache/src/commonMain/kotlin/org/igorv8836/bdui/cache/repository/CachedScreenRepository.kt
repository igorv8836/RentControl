package org.igorv8836.bdui.cache.repository

import org.igorv8836.bdui.cache.config.CacheKind
import org.igorv8836.bdui.cache.config.CachePolicy
import org.igorv8836.bdui.cache.store.CachedScreenEntry
import org.igorv8836.bdui.cache.store.MemoryScreenCacheStore
import org.igorv8836.bdui.cache.store.ScreenCacheStore
import org.igorv8836.bdui.common.time.currentTimeMillis
import org.igorv8836.bdui.runtime.ScreenRepository
import org.igorv8836.bdui.contract.RemoteScreen

/**
 * Repository decorator that adds caching (memory/disk) with TTL.
 */
class CachedScreenRepository(
    private val delegate: ScreenRepository,
    private val defaultPolicy: CachePolicy = CachePolicy(),
    private val memoryStore: ScreenCacheStore = MemoryScreenCacheStore(),
    private val diskStore: ScreenCacheStore? = null,
    private val policyResolver: (screenId: String, params: Map<String, String>) -> CachePolicy = { _, _ -> defaultPolicy },
) : ScreenRepository {

    override suspend fun fetch(screenId: String, params: Map<String, String>): Result<RemoteScreen> {
        val policy = policyResolver(screenId, params)
        if (!policy.enabled) return delegate.fetch(screenId, params)

        val store = when (policy.kind) {
            CacheKind.Memory -> memoryStore
            CacheKind.Disk -> diskStore ?: memoryStore
        }
        val key = cacheKey(screenId, params)
        val now = currentTimeMillis()
        store.clearExpired(now)

        val cached = store.get(key)
        if (cached != null && (cached.expiresAtMillis == null || cached.expiresAtMillis > now)) {
            return Result.success(cached.remoteScreen)
        }

        val fetched = delegate.fetch(screenId, params)
        fetched.getOrNull()?.let { screen ->
            val expiresAt = policy.ttlMillis?.let { now + it }
            store.put(key, CachedScreenEntry(screen, expiresAt))
        }
        return fetched
    }

    private fun cacheKey(screenId: String, params: Map<String, String>): String {
        if (params.isEmpty()) return screenId
        val sorted = params.toSortedMap()
        val suffix = sorted.entries.joinToString("&") { "${it.key}=${it.value}" }
        return "$screenId?$suffix"
    }
}

package org.igorv8836.bdui.cache.store

class MemoryScreenCacheStore : ScreenCacheStore {
    private val memory: MutableMap<String, CachedScreenEntry> = mutableMapOf()

    override suspend fun get(key: String): CachedScreenEntry? = memory[key]

    override suspend fun put(key: String, entry: CachedScreenEntry) {
        memory[key] = entry
    }

    override suspend fun remove(key: String) {
        memory.remove(key)
    }

    override suspend fun clearExpired(now: Long) {
        val expired = memory.filterValues { entry ->
            entry.expiresAtMillis?.let { it < now } ?: false
        }.keys
        expired.forEach { memory.remove(it) }
    }
}

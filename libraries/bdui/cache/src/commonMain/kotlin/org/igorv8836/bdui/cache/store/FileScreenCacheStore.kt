package org.igorv8836.bdui.cache.store

import org.igorv8836.bdui.contract.Screen

class FileScreenCacheStore(
    private val storage: DiskStorage,
    private val encode: (Screen) -> String,
    private val decode: (String) -> Screen,
) : ScreenCacheStore {
    override suspend fun get(key: String): CachedScreenEntry? {
        val text = storage.read(key) ?: return null
        val parts = text.split("\n", limit = 2)
        if (parts.size != 2) return null
        val expiresAt = parts[0].toLongOrNull()
        val payload = parts[1]
        return CachedScreenEntry(
            screen = decode(payload),
            expiresAtMillis = expiresAt,
        )
    }

    override suspend fun put(key: String, entry: CachedScreenEntry) {
        val header = entry.expiresAtMillis?.toString() ?: "-1"
        val payload = encode(entry.screen)
        storage.write(key, "$header\n$payload")
    }

    override suspend fun remove(key: String) {
        storage.delete(key)
    }

    override suspend fun clearExpired(now: Long) {
        storage.listKeys().forEach { key ->
            val entry = get(key)
            if (entry?.expiresAtMillis?.let { it < now } == true) {
                remove(key)
            }
        }
    }
}

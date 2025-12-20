package org.igorv8836.bdui.network.repository

import org.igorv8836.bdui.contract.RemoteScreen
import org.igorv8836.bdui.cache.config.CachePolicy
import org.igorv8836.bdui.cache.repository.CachedScreenRepository
import org.igorv8836.bdui.cache.store.DiskStorage
import org.igorv8836.bdui.cache.store.FileScreenCacheStore
import org.igorv8836.bdui.cache.store.MemoryScreenCacheStore
import org.igorv8836.bdui.cache.store.ScreenCacheStore
import org.igorv8836.bdui.network.datasource.RemoteScreenDataSource
import org.igorv8836.bdui.runtime.ScreenRepository

/**
 * Utility to compose network + cache into a single [ScreenRepository].
 *
 * @param remote remote data source for screens
 * @param cachePolicy default cache policy (can be overridden per-request via [policyResolver])
 * @param diskStorage optional disk storage path/provider; if null and [cachePolicy.kind] is Disk, memory will be used
 * @param encode serialization from [RemoteScreen] to String for disk cache
 * @param decode deserialization from String to [RemoteScreen] for disk cache
 * @param policyResolver optional resolver to vary cache policy by screen/params
 */
fun buildScreenRepository(
    remote: RemoteScreenDataSource,
    cachePolicy: CachePolicy = CachePolicy(),
    diskStorage: DiskStorage? = null,
    encode: (RemoteScreen) -> String,
    decode: (String) -> RemoteScreen,
    policyResolver: (screenId: String, params: Map<String, String>) -> CachePolicy = { _, _ -> cachePolicy },
): ScreenRepository {
    val base = NetworkScreenRepository(remote)
    val memoryStore: ScreenCacheStore = MemoryScreenCacheStore()
    val diskStore: ScreenCacheStore? = diskStorage?.let { storage ->
        FileScreenCacheStore(storage, encode, decode)
    }
    return CachedScreenRepository(
        delegate = base,
        defaultPolicy = cachePolicy,
        memoryStore = memoryStore,
        diskStore = diskStore,
        policyResolver = policyResolver,
    )
}

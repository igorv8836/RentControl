package org.igorv8836.bdui.backend.runtime.engine

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.igorv8836.bdui.backend.core.BackendResult

/**
 * Источник данных для мапперов. Может быть вызван несколько раз, результаты кэшируются.
 */
interface Fetcher<R> {
    suspend fun fetch(ctx: FetcherContext): BackendResult<R>
}

class FetcherContext {
    private val cache = mutableMapOf<Class<out Fetcher<*>>, Deferred<BackendResult<Any?>>>()

    suspend fun <R> fetch(fetcher: Fetcher<R>): BackendResult<R> = coroutineScope {
        val key = fetcher.javaClass
        val deferred = cache.getOrPut(key) {
            async { fetcher.fetch(this@FetcherContext) as BackendResult<Any?> }
        }
        @Suppress("UNCHECKED_CAST")
        deferred.await() as BackendResult<R>
    }
}

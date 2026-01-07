package org.igorv8836.bdui.backend.runtime.engine

import org.igorv8836.bdui.backend.core.BackendResult

/**
 * Mapper для драфта секции или скаффолда.
 */
interface DraftMapper<D : Draft, R : RenderingData> {
    suspend fun map(draft: D, params: Parameters, fetchers: FetcherContext): BackendResult<R>
}

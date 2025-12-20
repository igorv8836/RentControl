package org.igorv8836.bdui.network.repository

import org.igorv8836.bdui.contract.Screen
import org.igorv8836.bdui.network.datasource.RemoteScreenDataSource
import org.igorv8836.bdui.runtime.ScreenRepository

/**
 * Default repository that delegates to [RemoteScreenDataSource].
 * The screenId is treated as remote endpoint identifier (path or id).
 */
class NetworkScreenRepository(
    private val remote: RemoteScreenDataSource,
) : ScreenRepository {
    override suspend fun fetch(
        screenId: String,
        params: Map<String, String>,
    ): Result<Screen> = remote.fetch(screenId, params)
}

package org.igorv8836.bdui.network.datasource

import org.igorv8836.bdui.contract.Screen

/**
 * Abstraction over network access for loading screens from backend.
 * Implementations can wrap ktor/OkHttp/URLSession; keeping narrow API keeps runtime modular.
 */
interface RemoteScreenDataSource {
    suspend fun fetch(screenId: String, params: Map<String, String> = emptyMap()): Result<Screen>
}

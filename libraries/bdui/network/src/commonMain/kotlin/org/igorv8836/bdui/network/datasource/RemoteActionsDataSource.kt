package org.igorv8836.bdui.network.datasource

import org.igorv8836.bdui.contract.Action

/**
 * Abstraction for loading remote actions (RemoteAction response).
 */
interface RemoteActionsDataSource {
    suspend fun fetch(path: String, params: Map<String, String> = emptyMap()): Result<List<Action>>
}

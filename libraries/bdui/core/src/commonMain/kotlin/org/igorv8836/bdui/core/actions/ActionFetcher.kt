package org.igorv8836.bdui.core.actions

import org.igorv8836.bdui.contract.Action

/**
 * Abstraction for loading actions from a remote source.
 * Used by RemoteAction to fetch a list of follow-up actions.
 */
interface ActionFetcher {
    suspend fun fetch(path: String, parameters: Map<String, String> = emptyMap()): Result<List<Action>>
}

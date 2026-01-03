package org.igorv8836.bdui.network.repository

import org.igorv8836.bdui.contract.Action
import org.igorv8836.bdui.core.actions.ActionFetcher
import org.igorv8836.bdui.network.datasource.RemoteActionsDataSource

class RemoteActionsFetcher(
    private val dataSource: RemoteActionsDataSource,
) : ActionFetcher {
    override suspend fun fetch(path: String, parameters: Map<String, String>): Result<List<Action>> =
        dataSource.fetch(path, parameters)
}

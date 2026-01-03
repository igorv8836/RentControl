package org.igorv8836.bdui.actions.navigation

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.igorv8836.bdui.contract.Action
import org.igorv8836.bdui.core.actions.ActionFetcher
import org.igorv8836.bdui.core.actions.ActionHandler
import org.igorv8836.bdui.core.context.ActionContext
import org.igorv8836.bdui.logger.ConsoleLogger
import org.igorv8836.bdui.logger.LogTags
import org.igorv8836.bdui.logger.Logger

@Serializable
@SerialName("RemoteAction")
data class RemoteAction(
    override val id: String,
    val path: String,
    val parameters: Map<String, String> = emptyMap(),
) : Action

class RemoteActionHandler(
    private val logger: Logger = ConsoleLogger(LogTags.RUNTIME),
) : ActionHandler<RemoteAction> {
    override suspend fun handle(action: RemoteAction, context: ActionContext) {
        val fetcher: ActionFetcher = context.actionFetcher ?: run {
            logger.warn("RemoteAction: no fetcher provided, skipping id=${action.id}")
            return
        }
        val registry = context.screenContext.actionRegistry
        val result = fetcher.fetch(action.path, action.parameters)
        val actions = result.getOrElse { throwable ->
            logger.error("RemoteAction fetch failed: ${throwable.message}")
            return
        }
        actions.forEach { next ->
            registry.dispatch(next, context)
        }
    }
}

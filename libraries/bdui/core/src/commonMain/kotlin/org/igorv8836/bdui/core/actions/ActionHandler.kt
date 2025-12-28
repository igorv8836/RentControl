package org.igorv8836.bdui.core.actions

import org.igorv8836.bdui.contract.Action
import org.igorv8836.bdui.core.context.ActionContext
import org.igorv8836.bdui.logger.ConsoleLogger
import org.igorv8836.bdui.logger.LogMessages
import org.igorv8836.bdui.logger.LogTags
import org.igorv8836.bdui.logger.formatLog
import org.igorv8836.bdui.logger.Logger
import kotlin.reflect.KClass

fun interface ActionHandler<in T : Action> {
    suspend fun handle(action: T, context: ActionContext)
}

class ActionRegistry(
    private val handlersByType: Map<KClass<out Action>, ActionHandler<*>>,
    private val logger: Logger = ConsoleLogger(LogTags.ACTIONS),
) {
    suspend fun dispatch(action: Action, context: ActionContext) {
        val handler = handlersByType[action::class]
        if (handler == null) {
            logger.warn(formatLog(LogMessages.MISSING_HANDLER, action::class.simpleName ?: "", action.id))
            return
        }
        @Suppress("UNCHECKED_CAST")
        (handler as? ActionHandler<Action>)?.handle(action, context) ?: run {
            logger.error(formatLog(LogMessages.HANDLER_MISMATCH, action::class.simpleName ?: ""))
        }
    }
}

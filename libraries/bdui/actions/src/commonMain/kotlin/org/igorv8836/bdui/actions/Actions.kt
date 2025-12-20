package org.igorv8836.bdui.actions

import org.igorv8836.bdui.contract.Action
import org.igorv8836.bdui.contract.Route

data class ActionContext(
    val router: Router,
    val analytics: (String, Map<String, String>) -> Unit = { _, _ -> },
)

interface Router {
    fun navigate(route: Route)
}

fun interface ActionHandler {
    suspend fun handle(action: Action, context: ActionContext)
}

class ActionRegistry(
    private val handlers: Map<String, ActionHandler>,
    private val fallback: ActionHandler? = null,
) {
    suspend fun dispatch(action: Action, context: ActionContext) {
        val handler = handlers[action.id] ?: fallback
        handler?.handle(action, context)
    }
}

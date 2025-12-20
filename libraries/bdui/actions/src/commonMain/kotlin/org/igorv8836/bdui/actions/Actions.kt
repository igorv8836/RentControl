package org.igorv8836.bdui.actions

import org.igorv8836.bdui.contract.Action
import org.igorv8836.bdui.contract.Route

data class ActionContext(
    val router: Router,
    val analytics: (String, Map<String, String>) -> Unit = { _, _ -> },
    val navigator: Navigator? = null,
    val variables: VariableAdapter? = null,
    val screenId: String? = null,
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
    executor: ActionExecutor? = null,
) {
    private val defaultHandler: ActionHandler? = executor?.let { exec ->
        ActionHandler { action, context -> exec.execute(action, context) }
    }

    suspend fun dispatch(action: Action, context: ActionContext) {
        val handler = handlers[action.id] ?: fallback ?: defaultHandler
        handler?.handle(action, context)
    }
}

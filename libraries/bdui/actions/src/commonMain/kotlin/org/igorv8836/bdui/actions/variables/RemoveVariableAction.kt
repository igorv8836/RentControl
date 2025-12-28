package org.igorv8836.bdui.actions.variables

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.igorv8836.bdui.contract.Action
import org.igorv8836.bdui.contract.VariableScope
import org.igorv8836.bdui.core.actions.ActionHandler
import org.igorv8836.bdui.core.context.ActionContext

@Serializable
@SerialName("RemoveVariableAction")
data class RemoveVariableAction(
    override val id: String,
    val key: String,
    val scope: VariableScope = VariableScope.Global,
    val screenId: String? = null,
) : Action

class RemoveVariableActionHandler : ActionHandler<RemoveVariableAction> {
    override suspend fun handle(action: RemoveVariableAction, context: ActionContext) {
        context.screenContext.variableStore.remove(
            key = action.key,
            scope = action.scope,
            screenId = action.screenId,
        )
    }
}

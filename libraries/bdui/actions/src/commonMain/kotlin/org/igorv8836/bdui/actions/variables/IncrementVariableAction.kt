package org.igorv8836.bdui.actions.variables

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.igorv8836.bdui.contract.Action
import org.igorv8836.bdui.contract.StoragePolicy
import org.igorv8836.bdui.contract.VariableScope
import org.igorv8836.bdui.core.actions.ActionHandler
import org.igorv8836.bdui.core.context.ActionContext

@Serializable
@SerialName("IncrementVariableAction")
data class IncrementVariableAction(
    override val id: String,
    val key: String,
    val delta: Double = 1.0,
    val scope: VariableScope = VariableScope.Global,
    val policy: StoragePolicy = StoragePolicy.InMemory,
    val screenId: String? = null,
) : Action

class IncrementVariableActionHandler : ActionHandler<IncrementVariableAction> {
    override suspend fun handle(action: IncrementVariableAction, context: ActionContext) {
        context.screenContext.variableStore.increment(
            key = action.key,
            delta = action.delta,
            scope = action.scope,
            screenId = action.screenId,
            policy = action.policy,
        )
    }
}

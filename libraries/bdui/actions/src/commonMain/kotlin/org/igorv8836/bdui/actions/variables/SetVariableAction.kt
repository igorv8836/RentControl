package org.igorv8836.bdui.actions.variables

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.igorv8836.bdui.contract.Action
import org.igorv8836.bdui.contract.StoragePolicy
import org.igorv8836.bdui.contract.VariableScope
import org.igorv8836.bdui.contract.VariableValue
import org.igorv8836.bdui.core.actions.ActionHandler
import org.igorv8836.bdui.core.context.ActionContext

@Serializable
@SerialName("SetVariableAction")
data class SetVariableAction(
    override val id: String,
    val key: String,
    val value: VariableValue,
    val scope: VariableScope = VariableScope.Global,
    val policy: StoragePolicy = StoragePolicy.InMemory,
    val ttlMillis: Long? = null,
    val screenId: String? = null,
) : Action

class SetVariableActionHandler : ActionHandler<SetVariableAction> {
    override suspend fun handle(action: SetVariableAction, context: ActionContext) {
        context.screenContext.variableStore.set(
            key = action.key,
            scope = action.scope,
            value = action.value,
            screenId = action.screenId,
            policy = action.policy,
            ttlMillis = action.ttlMillis,
        )
    }
}

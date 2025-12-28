package org.igorv8836.bdui.actions.navigation

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.igorv8836.bdui.contract.Action
import org.igorv8836.bdui.contract.RemoteScreen
import org.igorv8836.bdui.contract.RoutePresentation
import org.igorv8836.bdui.core.actions.ActionHandler
import org.igorv8836.bdui.core.context.ActionContext

@Serializable
@SerialName("ForwardAction")
data class ForwardAction(
    override val id: String,
    val path: String? = null,
    val remoteScreen: RemoteScreen? = null,
    val presentation: RoutePresentation = RoutePresentation.Push,
    val parameters: Map<String, String> = emptyMap(),
) : Action

class ForwardActionHandler : ActionHandler<ForwardAction> {
    override suspend fun handle(action: ForwardAction, context: ActionContext) {
        context.navigator.forward(
            path = action.path,
            remoteScreen = action.remoteScreen,
            parameters = action.parameters,
        )
    }
}

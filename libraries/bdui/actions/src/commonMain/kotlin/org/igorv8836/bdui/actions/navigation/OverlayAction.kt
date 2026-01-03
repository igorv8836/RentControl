package org.igorv8836.bdui.actions.navigation

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.igorv8836.bdui.contract.Action
import org.igorv8836.bdui.contract.Overlay
import org.igorv8836.bdui.contract.RemoteScreen
import org.igorv8836.bdui.core.actions.ActionHandler
import org.igorv8836.bdui.core.context.ActionContext

@Serializable
@SerialName("OverlayAction")
data class OverlayAction(
    override val id: String,
    val overlay: Overlay,
    val path: String? = null,
    val remoteScreen: RemoteScreen? = null,
    val parameters: Map<String, String> = emptyMap(),
) : Action

class OverlayActionHandler : ActionHandler<OverlayAction> {
    override suspend fun handle(action: OverlayAction, context: ActionContext) {
        action.remoteScreen?.let {
            context.navigator.forward(remoteScreen = it, parameters = action.parameters)
        }
        context.navigator.showOverlay(
            overlay = action.overlay,
            parameters = action.parameters,
        )
        action.path?.let {
            context.navigator.forward(path = it, parameters = action.parameters)
        }
    }
}

package org.igorv8836.bdui.actions.navigation

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.igorv8836.bdui.contract.Action
import org.igorv8836.bdui.contract.Overlay
import org.igorv8836.bdui.core.actions.ActionHandler
import org.igorv8836.bdui.core.context.ActionContext

@Serializable
@SerialName("OverlayAction")
data class OverlayAction(
    override val id: String,
    val overlay: Overlay,
    val parameters: Map<String, String> = emptyMap(),
) : Action

class OverlayActionHandler : ActionHandler<OverlayAction> {
    override suspend fun handle(action: OverlayAction, context: ActionContext) {
        context.navigator.showOverlay(
            overlay = action.overlay,
            parameters = action.parameters,
        )
    }
}

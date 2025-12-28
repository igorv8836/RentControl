package org.igorv8836.bdui.actions.navigation

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.igorv8836.bdui.contract.Action
import org.igorv8836.bdui.contract.Popup
import org.igorv8836.bdui.core.actions.ActionHandler
import org.igorv8836.bdui.core.context.ActionContext

@Serializable
@SerialName("PopupAction")
data class PopupAction(
    override val id: String,
    val popup: Popup,
    val parameters: Map<String, String> = emptyMap(),
) : Action

class PopupActionHandler : ActionHandler<PopupAction> {
    override suspend fun handle(action: PopupAction, context: ActionContext) {
        context.navigator.showPopup(
            popup = action.popup,
            parameters = action.parameters,
        )
    }
}

package org.igorv8836.bdui.actions

import org.igorv8836.bdui.contract.Action
import org.igorv8836.bdui.contract.AnalyticsAction
import org.igorv8836.bdui.contract.CustomAction
import org.igorv8836.bdui.contract.ForwardAction
import org.igorv8836.bdui.contract.Overlay
import org.igorv8836.bdui.contract.OverlayAction
import org.igorv8836.bdui.contract.Popup
import org.igorv8836.bdui.contract.PopupAction
import org.igorv8836.bdui.contract.Route
import org.igorv8836.bdui.contract.Screen
import org.igorv8836.bdui.contract.SubmitAction

interface Navigator {
    fun openRoute(route: Route, parameters: Map<String, String> = emptyMap())
    fun forward(path: String? = null, screen: Screen? = null, parameters: Map<String, String> = emptyMap())
    fun showPopup(popup: Popup, parameters: Map<String, String> = emptyMap())
    fun showOverlay(overlay: Overlay, parameters: Map<String, String> = emptyMap())
}

data class ActionValidationConfig(
    val allowedSchemes: Set<String> = setOf("http", "https"),
    val maxPathLength: Int = 2048,
)

class ActionValidator(
    private val config: ActionValidationConfig = ActionValidationConfig(),
) {
    fun validateForwardPath(path: String?): Result<Unit> {
        if (path == null) return Result.success(Unit)
        if (path.length > config.maxPathLength) {
            return Result.failure(IllegalArgumentException("Path too long"))
        }
        val scheme = path.substringBefore("://", missingDelimiterValue = "")
        if (scheme.isNotEmpty() && scheme !in config.allowedSchemes) {
            return Result.failure(IllegalArgumentException("Scheme '$scheme' not allowed"))
        }
        return Result.success(Unit)
    }
}

class ActionExecutor(
    private val navigator: Navigator,
    private val analytics: (String, Map<String, String>) -> Unit = { _, _ -> },
    private val validator: ActionValidator = ActionValidator(),
) {
    suspend fun execute(action: Action, context: ActionContext) {
        when (action) {
            is ForwardAction -> {
                validator.validateForwardPath(action.path).getOrThrow()
                navigator.forward(path = action.path, screen = action.screen, parameters = action.parameters)
            }

            is PopupAction -> {
                navigator.showPopup(action.popup, action.parameters)
            }

            is OverlayAction -> {
                navigator.showOverlay(action.overlay, action.parameters)
            }

            is AnalyticsAction -> {
                analytics(action.analytics.event, action.analytics.params)
            }

            is SubmitAction -> {
                analytics("submit", action.payload)
            }

            is CustomAction -> {
                analytics("custom_${action.name}", action.parameters)
            }
        }
    }
}

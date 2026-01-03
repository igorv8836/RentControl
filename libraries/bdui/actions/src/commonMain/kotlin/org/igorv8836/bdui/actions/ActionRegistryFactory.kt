package org.igorv8836.bdui.actions

import org.igorv8836.bdui.actions.generic.AnalyticsAction
import org.igorv8836.bdui.actions.generic.AnalyticsActionHandler
import org.igorv8836.bdui.actions.generic.CustomAction
import org.igorv8836.bdui.actions.generic.CustomActionHandler
import org.igorv8836.bdui.actions.generic.SubmitAction
import org.igorv8836.bdui.actions.generic.SubmitActionHandler
import org.igorv8836.bdui.actions.navigation.ForwardAction
import org.igorv8836.bdui.actions.navigation.ForwardActionHandler
import org.igorv8836.bdui.actions.navigation.OverlayAction
import org.igorv8836.bdui.actions.navigation.OverlayActionHandler
import org.igorv8836.bdui.actions.navigation.PopupAction
import org.igorv8836.bdui.actions.navigation.PopupActionHandler
import org.igorv8836.bdui.actions.navigation.RemoteAction
import org.igorv8836.bdui.actions.navigation.RemoteActionHandler
import org.igorv8836.bdui.actions.variables.IncrementVariableAction
import org.igorv8836.bdui.actions.variables.IncrementVariableActionHandler
import org.igorv8836.bdui.actions.variables.RemoveVariableAction
import org.igorv8836.bdui.actions.variables.RemoveVariableActionHandler
import org.igorv8836.bdui.actions.variables.SetVariableAction
import org.igorv8836.bdui.actions.variables.SetVariableActionHandler
import org.igorv8836.bdui.contract.Action
import org.igorv8836.bdui.core.actions.ActionHandler
import org.igorv8836.bdui.core.actions.ActionRegistry
import kotlin.reflect.KClass

fun defaultActionHandlers(): Map<KClass<out Action>, ActionHandler<*>> = mapOf(
    SubmitAction::class to SubmitActionHandler(),
    AnalyticsAction::class to AnalyticsActionHandler(),
    CustomAction::class to CustomActionHandler(),
    ForwardAction::class to ForwardActionHandler(),
    PopupAction::class to PopupActionHandler(),
    OverlayAction::class to OverlayActionHandler(),
    RemoteAction::class to RemoteActionHandler(),
    SetVariableAction::class to SetVariableActionHandler(),
    IncrementVariableAction::class to IncrementVariableActionHandler(),
    RemoveVariableAction::class to RemoveVariableActionHandler(),
)

fun buildActionRegistry(
    customHandlers: Map<KClass<out Action>, ActionHandler<*>> = emptyMap(),
): ActionRegistry {
    val merged = defaultActionHandlers().toMutableMap()
    merged.putAll(customHandlers)
    return ActionRegistry(merged)
}

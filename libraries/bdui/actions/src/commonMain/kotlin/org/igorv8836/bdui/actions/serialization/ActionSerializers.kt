package org.igorv8836.bdui.actions.serialization

import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.igorv8836.bdui.actions.generic.AnalyticsAction
import org.igorv8836.bdui.actions.generic.CustomAction
import org.igorv8836.bdui.actions.generic.SubmitAction
import org.igorv8836.bdui.actions.navigation.ForwardAction
import org.igorv8836.bdui.actions.navigation.OverlayAction
import org.igorv8836.bdui.actions.navigation.PopupAction
import org.igorv8836.bdui.actions.variables.IncrementVariableAction
import org.igorv8836.bdui.actions.variables.RemoveVariableAction
import org.igorv8836.bdui.actions.variables.SetVariableAction
import org.igorv8836.bdui.contract.Action

object ActionSerializers {
    val module = SerializersModule {
        polymorphic(Action::class) {
            subclass(SubmitAction::class)
            subclass(AnalyticsAction::class)
            subclass(CustomAction::class)
            subclass(ForwardAction::class)
            subclass(PopupAction::class)
            subclass(OverlayAction::class)
            subclass(SetVariableAction::class)
            subclass(IncrementVariableAction::class)
            subclass(RemoveVariableAction::class)
        }
    }
}

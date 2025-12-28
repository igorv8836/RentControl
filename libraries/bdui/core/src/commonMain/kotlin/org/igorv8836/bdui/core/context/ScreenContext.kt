package org.igorv8836.bdui.core.context

import org.igorv8836.bdui.core.actions.ActionRegistry
import org.igorv8836.bdui.core.variables.VariableStore

data class ScreenContext(
    val variableStore: VariableStore,
    val actionRegistry: ActionRegistry,
)
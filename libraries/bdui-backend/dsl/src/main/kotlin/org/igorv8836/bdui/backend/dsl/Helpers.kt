package org.igorv8836.bdui.backend.dsl

import org.igorv8836.bdui.contract.Action
import org.igorv8836.bdui.contract.Binding
import org.igorv8836.bdui.contract.Condition
import org.igorv8836.bdui.contract.MissingVariableBehavior
import org.igorv8836.bdui.contract.SetVariableAction
import org.igorv8836.bdui.contract.Trigger
import org.igorv8836.bdui.contract.VariableChanged
import org.igorv8836.bdui.contract.VariableScope
import org.igorv8836.bdui.contract.VariableValue

/**
 * Small preset: bind variable into text by key.
 */
fun bindText(
    variable: String,
    scope: VariableScope = VariableScope.Global,
    default: VariableValue? = null,
    missingBehavior: MissingVariableBehavior = MissingVariableBehavior.Empty,
): Binding = Binding(
    key = variable,
    scope = scope,
    default = default,
    missingBehavior = missingBehavior,
)

/**
 * Helper to define a simple variable mutation action inline.
 */
fun setVariableAction(
    id: String,
    key: String,
    value: VariableValue,
    scope: VariableScope = VariableScope.Global,
): SetVariableAction = SetVariableAction(
    id = id,
    key = key,
    value = value,
    scope = scope,
)

/**
 * Helper trigger builder for variable changes.
 */
fun variableChangedTrigger(
    id: String,
    key: String,
    scope: VariableScope = VariableScope.Global,
    actions: List<Action>,
    condition: Condition? = null,
): Trigger = Trigger(
    id = id,
    source = VariableChanged(key = key, scope = scope),
    condition = condition,
    actions = actions,
)

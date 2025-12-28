package org.igorv8836.bdui.renderer.binding

import org.igorv8836.bdui.contract.Condition
import org.igorv8836.bdui.contract.VariableScope
import org.igorv8836.bdui.contract.VariableValue
import org.igorv8836.bdui.core.variables.VariableStore

internal class BindingResolver(
    private val variables: VariableStore,
    private val screenId: String,
) {
    fun text(key: String?, template: String?): String {
        val base = template ?: key?.takeIf { it.isNotBlank() }
        return base?.let { interpolate(it) } ?: ""
    }

    fun isVisible(condition: Condition?): Boolean {
        if (condition == null) return true
        val value = resolveValue(condition.key)
        val exists = value != null
        if (!exists) {
            return !condition.exists
        }
        var result = condition.equals?.let { equalsValue(value, it) } ?: isTruthy(value)
        if (condition.negate) result = !result
        return result
    }

    fun isEnabled(base: Boolean, condition: Condition?): Boolean {
        if (!base) return false
        return isVisible(condition)
    }

    private fun interpolate(template: String): String {
        val regex = Regex("@\\{\\s*(.*?)\\s*\\}")
        return regex.replace(template) { match ->
            val key = match.groupValues.getOrNull(1)?.trim().orEmpty()
            val resolved = resolveValue(key)
            resolved?.let { valueToString(it) } ?: ""
        }
    }

    private fun isTruthy(value: VariableValue): Boolean =
        when (value) {
            is VariableValue.BoolValue -> value.value
            is VariableValue.NumberValue -> value.value != 0.0
            is VariableValue.StringValue -> value.value.isNotEmpty()
            is VariableValue.ObjectValue -> value.value.isNotEmpty()
        }

    private fun equalsValue(left: VariableValue, right: VariableValue): Boolean =
        when {
            left is VariableValue.StringValue && right is VariableValue.StringValue -> left.value == right.value
            left is VariableValue.NumberValue && right is VariableValue.NumberValue -> left.value == right.value
            left is VariableValue.BoolValue && right is VariableValue.BoolValue -> left.value == right.value
            left is VariableValue.ObjectValue && right is VariableValue.ObjectValue -> left.value == right.value
            else -> false
        }

    private fun valueToString(value: VariableValue): String =
        when (value) {
            is VariableValue.StringValue -> value.value
            is VariableValue.NumberValue -> value.value.toString()
            is VariableValue.BoolValue -> value.value.toString()
            is VariableValue.ObjectValue -> value.value.entries.joinToString(
                separator = ", ",
                prefix = "{",
                postfix = "}",
            ) { (key, inner) -> "$key:${valueToString(inner)}" }
        }

    private fun resolveValue(key: String): VariableValue? {
        variables.peek(key, VariableScope.Screen, screenId)?.let { return it }
        return variables.peek(key, VariableScope.Global, null)
    }
}

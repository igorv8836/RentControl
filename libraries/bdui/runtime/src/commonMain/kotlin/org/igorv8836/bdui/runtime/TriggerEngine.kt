package org.igorv8836.bdui.runtime

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.igorv8836.bdui.actions.ActionContext
import org.igorv8836.bdui.actions.ActionRegistry
import org.igorv8836.bdui.actions.Navigator
import org.igorv8836.bdui.actions.Router
import org.igorv8836.bdui.actions.VariableAdapter
import org.igorv8836.bdui.contract.Condition
import org.igorv8836.bdui.contract.MissingVariableBehavior
import org.igorv8836.bdui.contract.ScreenEventType
import org.igorv8836.bdui.contract.Trigger
import org.igorv8836.bdui.contract.TriggerSource
import org.igorv8836.bdui.contract.VariableScope
import org.igorv8836.bdui.contract.VariableValue

class TriggerEngine(
    private val screenId: String,
    private val variableStore: VariableStore,
    private val variableAdapter: VariableAdapter,
    private val actionRegistry: ActionRegistry,
    private val router: Router,
    private val analytics: (String, Map<String, String>) -> Unit = { _, _ -> },
    private val navigator: Navigator? = null,
    externalScope: CoroutineScope,
) {
    private val job = SupervisorJob()
    private val scope = CoroutineScope(externalScope.coroutineContext + job)
    private var triggers: List<Trigger> = emptyList()
    private val guard: MutableMap<String, ExecutionGuard> = mutableMapOf()
    private val previousValues: MutableMap<VariableKey, VariableValue?> = mutableMapOf()

    fun start(triggers: List<Trigger>) {
        this.triggers = triggers
        observeVariables()
    }

    fun onEvent(type: ScreenEventType) {
        val matching = triggers.filter { trigger ->
            val source = trigger.source
            source is TriggerSource.ScreenEvent && source.type == type
        }
        matching.forEach { trigger ->
            scope.launch { executeIfAllowed(trigger) }
        }
    }

    fun stop() {
        job.cancel()
    }

    private fun observeVariables() {
        val variableTriggers = triggers.filter { it.source is TriggerSource.VariableChanged }
        if (variableTriggers.isEmpty()) return
        scope.launch {
            variableStore.changes.collect {
                variableTriggers.forEach { trigger ->
                    val source = trigger.source as TriggerSource.VariableChanged
                    val key = VariableKey(source.key, source.scope, source.screenId ?: screenId)
                    val newValue = variableStore.peek(key.key, key.scope, key.screenId)
                    val prev = previousValues[key]
                    if (prev != newValue) {
                        previousValues[key] = newValue
                        executeIfAllowed(trigger)
                    }
                }
            }
        }
    }

    private suspend fun executeIfAllowed(trigger: Trigger) {
        val now = currentTimeMillis()
        val g = guard.getOrPut(trigger.id) { ExecutionGuard() }
        if (trigger.maxExecutions > 0 && g.executions >= trigger.maxExecutions) return
        trigger.debounceMs?.let { debounce ->
            if (now - g.lastRunMs < debounce) return
        }
        trigger.throttleMs?.let { throttle ->
            if (now - g.lastRunMs < throttle) return
        }
        if (!checkCondition(trigger.condition)) return

        g.executions += 1
        g.lastRunMs = now
        trigger.actions.forEach { action ->
            actionRegistry.dispatch(
                action,
                ActionContext(
                    router = router,
                    analytics = analytics,
                    navigator = navigator,
                    variables = variableAdapter,
                    screenId = screenId,
                ),
            )
        }
    }

    private fun checkCondition(condition: Condition?): Boolean {
        condition ?: return true
        val value = resolveValue(condition.binding)
        val exists = value != null
        if (!exists) {
            return !condition.exists
        }
        var result = condition.equals?.let { compareValues(value!!, it) } ?: isTruthy(value!!)
        if (condition.negate) result = !result
        return result
    }

    private fun resolveValue(binding: org.igorv8836.bdui.contract.Binding): VariableValue? {
        val value = variableStore.peek(binding.key, binding.scope, screenId)
        return when {
            value != null -> value
            binding.missingBehavior == MissingVariableBehavior.Default -> binding.default
            binding.missingBehavior == MissingVariableBehavior.Error -> throw IllegalStateException("Variable '${binding.key}' is missing")
            else -> null
        }
    }

    private fun isTruthy(value: VariableValue): Boolean =
        when (value) {
            is VariableValue.BoolValue -> value.value
            is VariableValue.NumberValue -> value.value != 0.0
            is VariableValue.StringValue -> value.value.isNotEmpty()
            is VariableValue.ObjectValue -> value.value.isNotEmpty()
        }

    private fun compareValues(left: VariableValue, right: VariableValue): Boolean =
        when {
            left is VariableValue.StringValue && right is VariableValue.StringValue -> left.value == right.value
            left is VariableValue.NumberValue && right is VariableValue.NumberValue -> left.value == right.value
            left is VariableValue.BoolValue && right is VariableValue.BoolValue -> left.value == right.value
            left is VariableValue.ObjectValue && right is VariableValue.ObjectValue -> left.value == right.value
            else -> false
        }

    private data class VariableKey(
        val key: String,
        val scope: VariableScope,
        val screenId: String?,
    )

    private data class ExecutionGuard(
        var executions: Int = 0,
        var lastRunMs: Long = 0,
    )
}

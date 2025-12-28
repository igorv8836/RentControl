package org.igorv8836.bdui.runtime

import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.igorv8836.bdui.contract.Trigger
import org.igorv8836.bdui.contract.TriggerSource
import org.igorv8836.bdui.contract.VariableScope
import org.igorv8836.bdui.contract.VariableValue
import org.igorv8836.bdui.contract.StoragePolicy
import org.igorv8836.bdui.core.actions.ActionHandler
import org.igorv8836.bdui.core.actions.ActionRegistry
import org.igorv8836.bdui.core.context.ActionContext
import org.igorv8836.bdui.core.context.ScreenContext
import org.igorv8836.bdui.testing.TestNavigator
import kotlin.test.Test
import kotlin.test.assertEquals

private data class TestAction(override val id: String = "a1") : org.igorv8836.bdui.contract.Action

class TriggerEngineTest {

    @Test
    fun variableChangeFiresTrigger() = runTest {
        var calls = 0
        val registry = ActionRegistry(
            handlersByType = mapOf(
                TestAction::class to ActionHandler<TestAction> { _, _ -> calls++ },
            ),
        )
        val store = VariableStoreImpl(scope = this, enableSync = false)
        val screenContext = ScreenContext(variableStore = store, actionRegistry = registry)
        val actionContext = ActionContext(
            navigator = TestNavigator,
            screenContext = screenContext,
            screenId = "home",
        )
        val engine = TriggerEngine(
            screenId = "home",
            variableStore = store,
            actionContext = actionContext,
            externalScope = this,
        )
        engine.start(
            listOf(
                Trigger(
                    id = "t1",
                    source = TriggerSource.VariableChanged(key = "flag", scope = VariableScope.Global),
                    actions = listOf(TestAction()),
                ),
            ),
        )

        store.set(
            key = "flag",
            value = VariableValue.StringValue("yes"),
            scope = VariableScope.Global,
            screenId = "home",
            policy = StoragePolicy.InMemory,
            ttlMillis = null,
        )

        advanceUntilIdle()
        store.dispose()

        assertEquals(1, calls)
    }
}

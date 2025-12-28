package org.igorv8836.bdui.core.actions

import kotlinx.coroutines.runBlocking
import org.igorv8836.bdui.contract.Action
import org.igorv8836.bdui.testing.testActionContext
import kotlin.test.Test
import kotlin.test.assertEquals

private data class TestAction(override val id: String = "test") : Action

class ActionRegistryTest {

    @Test
    fun dispatchInvokesRegisteredHandler() = runBlocking {
        var calls = 0
        val registry = ActionRegistry(
            handlersByType = mapOf(
                TestAction::class to ActionHandler<TestAction> { _, _ -> calls++ },
            ),
        )

        registry.dispatch(TestAction(), testActionContext(registry))

        assertEquals(1, calls)
    }
}

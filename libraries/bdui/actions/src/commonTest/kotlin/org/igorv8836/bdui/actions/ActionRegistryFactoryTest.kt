package org.igorv8836.bdui.actions

import kotlinx.coroutines.test.runTest
import org.igorv8836.bdui.actions.navigation.ForwardAction
import org.igorv8836.bdui.core.actions.ActionHandler
import org.igorv8836.bdui.testing.testActionContext
import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ActionRegistryFactoryTest {

    @Test
    fun mergesCustomHandlersAndDispatches() = runTest {
        var calls = 0
        val override: ActionHandler<ForwardAction> = ActionHandler { _, _ -> calls++ }

        val registry = buildActionRegistry(
            customHandlers = mapOf<KClass<out org.igorv8836.bdui.contract.Action>, ActionHandler<*>>(
                ForwardAction::class to override,
            ),
        )

        registry.dispatch(ForwardAction(id = "go"), testActionContext(registry))

        assertEquals(1, calls)
    }

    @Test
    fun defaultHandlersCoverNavigationAndVariables() {
        val handlers = defaultActionHandlers()
        assertTrue(handlers.containsKey(ForwardAction::class))
        assertTrue(handlers.keys.any { it.simpleName?.contains("OverlayAction") == true })
        assertTrue(handlers.keys.any { it.simpleName?.contains("PopupAction") == true })
        assertTrue(handlers.keys.any { it.simpleName?.contains("SetVariableAction") == true })
        assertTrue(handlers.keys.any { it.simpleName?.contains("IncrementVariableAction") == true })
        assertTrue(handlers.keys.any { it.simpleName?.contains("RemoveVariableAction") == true })
    }
}

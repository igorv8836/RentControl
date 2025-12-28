package org.igorv8836.bdui.engine

import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.cancel
import org.igorv8836.bdui.contract.Layout
import org.igorv8836.bdui.contract.RemoteScreen
import org.igorv8836.bdui.contract.ScreenLifecycle
import org.igorv8836.bdui.contract.UiEvent
import org.igorv8836.bdui.contract.ComponentNode
import org.igorv8836.bdui.contract.TextElement
import org.igorv8836.bdui.core.actions.ActionHandler
import org.igorv8836.bdui.core.actions.ActionRegistry
import org.igorv8836.bdui.testing.TestNavigator
import org.igorv8836.bdui.runtime.ScreenRepository
import org.igorv8836.bdui.runtime.ScreenState
import org.igorv8836.bdui.runtime.VariableStoreImpl
import kotlin.test.Test
import kotlin.test.assertEquals

private data class LifecycleAction(override val id: String = "life") : org.igorv8836.bdui.contract.Action

class ScreenEngineIntegrationTest {

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun lifecycleActionsExecutedOnOpen() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val scope = CoroutineScope(SupervisorJob() + dispatcher)
        val calls = mutableListOf<String>()
        val registry = ActionRegistry(
            handlersByType = mapOf(
                LifecycleAction::class to ActionHandler<LifecycleAction> { action, _ ->
                    calls += action.id
                },
            ),
        )
        val screen = RemoteScreen(
            id = "home",
            version = 1,
            layout = Layout(root = TextElement(id = "t1", textKey = "hello")),
            actions = listOf(LifecycleAction()),
            lifecycle = ScreenLifecycle(
                onOpen = listOf(UiEvent(actions = listOf(LifecycleAction("life")), id = "")),
            ),
        )
        val repository = object : ScreenRepository {
            override suspend fun fetch(screenId: String, params: Map<String, String>): Result<RemoteScreen> =
                Result.success(screen)
        }
        val engine = ScreenEngineFactory(
            repository = repository,
            navigator = TestNavigator,
            actionRegistry = registry,
            variableStore = VariableStoreImpl(scope = scope, enableSync = false),
        ).create(screenId = "home", scope = scope)

        try {
            engine.onOpen()
            advanceUntilIdle()

            assertEquals(listOf("life"), calls)
            assertEquals(org.igorv8836.bdui.runtime.ScreenStatus.Ready, engine.state.value.status)
            assertEquals(screen.id, engine.state.value.remoteScreen?.id)
        } finally {
            engine.dispose()
            advanceUntilIdle()
            scope.cancel()
        }
    }
}

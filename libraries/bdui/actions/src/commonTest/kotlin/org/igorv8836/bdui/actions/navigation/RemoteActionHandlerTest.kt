package org.igorv8836.bdui.actions.navigation

import kotlinx.coroutines.test.runTest
import org.igorv8836.bdui.contract.Action
import org.igorv8836.bdui.core.actions.ActionFetcher
import org.igorv8836.bdui.core.actions.ActionRegistry
import org.igorv8836.bdui.core.context.ActionContext
import org.igorv8836.bdui.core.context.ScreenContext
import org.igorv8836.bdui.core.navigation.Navigator
import org.igorv8836.bdui.testing.TestVariableStore
import kotlin.test.Test
import kotlin.test.assertEquals

private class RecordingFetcher(private val result: Result<List<Action>>) : ActionFetcher {
    var calls = 0
    var lastPath: String? = null
    override suspend fun fetch(path: String, parameters: Map<String, String>): Result<List<Action>> {
        calls++
        lastPath = path
        return result
    }
}

private object NoopNavigator : Navigator {
    override fun openRoute(route: org.igorv8836.bdui.contract.Route, parameters: Map<String, String>) {}
    override fun forward(path: String?, remoteScreen: org.igorv8836.bdui.contract.RemoteScreen?, parameters: Map<String, String>) {}
    override fun showPopup(popup: org.igorv8836.bdui.contract.Popup, parameters: Map<String, String>) {}
    override fun showOverlay(overlay: org.igorv8836.bdui.contract.Overlay, parameters: Map<String, String>) {}
}

class RemoteActionHandlerTest {

    @Test
    fun executesFetchedActions() = runTest {
        val returned = listOf(ForwardAction(id = "next", path = "/home"))
        val fetcher = RecordingFetcher(Result.success(returned))
        val dispatched = mutableListOf<Action>()
        val registry = ActionRegistry(
            handlersByType = mapOf(
                ForwardAction::class to object : org.igorv8836.bdui.core.actions.ActionHandler<ForwardAction> {
                    override suspend fun handle(action: ForwardAction, context: ActionContext) {
                        dispatched += action
                    }
                },
            ),
        )
        val handler = RemoteActionHandler()
        val context = ActionContext(
            navigator = NoopNavigator,
            screenContext = ScreenContext(
                variableStore = TestVariableStore,
                actionRegistry = registry,
            ),
            screenId = "s1",
            actionFetcher = fetcher,
        )

        handler.handle(
            action = RemoteAction(id = "remote", path = "/login"),
            context = context,
        )

        assertEquals(1, fetcher.calls)
        assertEquals("/login", fetcher.lastPath)
        assertEquals(listOf("next"), dispatched.map { it.id })
    }
}

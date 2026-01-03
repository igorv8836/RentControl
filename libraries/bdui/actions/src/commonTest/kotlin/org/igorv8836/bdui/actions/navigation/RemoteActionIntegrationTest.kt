package org.igorv8836.bdui.actions.navigation

import kotlinx.coroutines.test.runTest
import org.igorv8836.bdui.actions.buildActionRegistry
import org.igorv8836.bdui.contract.Action
import org.igorv8836.bdui.core.actions.ActionFetcher
import org.igorv8836.bdui.core.context.ActionContext
import org.igorv8836.bdui.core.context.ScreenContext
import org.igorv8836.bdui.core.navigation.Navigator
import org.igorv8836.bdui.testing.TestVariableStore
import kotlin.test.Test
import kotlin.test.assertEquals

class RemoteActionIntegrationTest {

    @Test
    fun dispatch_remote_action_executes_fetched_forward_actions() = runTest {
        val navigatorForwardLog = mutableListOf<String?>()
        val navigator = object : Navigator {
            override fun openRoute(route: org.igorv8836.bdui.contract.Route, parameters: Map<String, String>) {}
            override fun forward(path: String?, remoteScreen: org.igorv8836.bdui.contract.RemoteScreen?, parameters: Map<String, String>) {
                navigatorForwardLog += path ?: remoteScreen?.id
            }
            override fun showPopup(popup: org.igorv8836.bdui.contract.Popup, parameters: Map<String, String>) {}
            override fun showOverlay(overlay: org.igorv8836.bdui.contract.Overlay, parameters: Map<String, String>) {}
        }

        val fetcher = object : ActionFetcher {
            var calls = 0
            var lastPath: String? = null
            override suspend fun fetch(path: String, parameters: Map<String, String>): Result<List<Action>> {
                calls++
                lastPath = path
                return Result.success(listOf(ForwardAction(id = "go", path = "/home")))
            }
        }

        val registry = buildActionRegistry()
        val context = ActionContext(
            navigator = navigator,
            screenContext = ScreenContext(
                variableStore = TestVariableStore,
                actionRegistry = registry,
            ),
            screenId = "test",
            actionFetcher = fetcher,
        )

        registry.dispatch(
            action = RemoteAction(id = "remote-login", path = "/auth/login"),
            context = context,
        )

        assertEquals(1, fetcher.calls)
        assertEquals("/auth/login", fetcher.lastPath)
        assertEquals(listOf<String?>("/home"), navigatorForwardLog)
    }
}

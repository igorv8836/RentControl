package org.igorv8836.bdui.actions.navigation

import kotlinx.coroutines.test.runTest
import org.igorv8836.bdui.contract.Popup
import org.igorv8836.bdui.contract.RemoteScreen
import org.igorv8836.bdui.contract.RoutePresentation
import org.igorv8836.bdui.contract.Overlay
import org.igorv8836.bdui.core.actions.ActionRegistry
import org.igorv8836.bdui.core.context.ActionContext
import org.igorv8836.bdui.core.context.ScreenContext
import org.igorv8836.bdui.core.navigation.Navigator
import org.igorv8836.bdui.contract.Layout
import org.igorv8836.bdui.contract.Container
import org.igorv8836.bdui.contract.ContainerDirection
import org.igorv8836.bdui.testing.TestVariableStore
import kotlin.test.Test
import kotlin.test.assertEquals

private class RecordingNavigator : Navigator {
    val calls = mutableListOf<String>()
    override fun openRoute(route: org.igorv8836.bdui.contract.Route, parameters: Map<String, String>) {
        calls += "route:${route.destination}"
    }

    override fun forward(path: String?, remoteScreen: RemoteScreen?, parameters: Map<String, String>) {
        if (remoteScreen != null) {
            calls += "forward:remote=${remoteScreen.id}"
        } else {
            calls += "forward:path=$path"
        }
    }

    override fun showPopup(popup: Popup, parameters: Map<String, String>) {
        calls += "popup"
    }

    override fun showOverlay(overlay: Overlay, parameters: Map<String, String>) {
        calls += "overlay"
    }
}

private fun actionContext(navigator: Navigator): ActionContext =
    ActionContext(
        navigator = navigator,
        screenContext = ScreenContext(
            variableStore = TestVariableStore,
            actionRegistry = ActionRegistry(emptyMap()),
        ),
        screenId = "test",
    )

class NavigationActionsTest {
    @Test
    fun forward_action_opens_preview_then_path() = runTest {
        val navigator = RecordingNavigator()
        val handler = ForwardActionHandler()
        val action = ForwardAction(
            id = "fwd",
            path = "/next",
            remoteScreen = RemoteScreen(
                id = "preview",
                version = 1,
                layout = Layout(
                    root = Container(
                        id = "root",
                        direction = ContainerDirection.Column,
                        children = emptyList()
                    )
                ),
                actions = emptyList(),
            ),
            presentation = RoutePresentation.Push,
            parameters = mapOf("k" to "v"),
        )

        handler.handle(action, actionContext(navigator))

        assertEquals(
            listOf("forward:remote=preview", "forward:path=/next"),
            navigator.calls,
        )
    }

    @Test
    fun popup_action_shows_preview_then_popup_then_path() = runTest {
        val navigator = RecordingNavigator()
        val handler = PopupActionHandler()
        val action = PopupAction(
            id = "popup",
            popup = Popup(),
            path = "/path",
            remoteScreen = RemoteScreen(
                id = "preview",
                version = 1,
                layout = Layout(
                    root = Container(
                        id = "root",
                        direction = ContainerDirection.Column,
                        children = emptyList()
                    )
                ),
                actions = emptyList(),
            ),
        )

        handler.handle(action, actionContext(navigator))

        assertEquals(
            listOf("forward:remote=preview", "popup", "forward:path=/path"),
            navigator.calls,
        )
    }

    @Test
    fun overlay_action_shows_preview_then_overlay_then_path() = runTest {
        val navigator = RecordingNavigator()
        val handler = OverlayActionHandler()
        val action = OverlayAction(
            id = "overlay",
            overlay = Overlay(),
            path = "/path",
            remoteScreen = RemoteScreen(
                id = "preview",
                version = 1,
                layout = Layout(
                    root = Container(
                        id = "root",
                        direction = ContainerDirection.Column,
                        children = emptyList()
                    )
                ),
                actions = emptyList(),
            ),
        )

        handler.handle(action, actionContext(navigator))

        assertEquals(
            listOf("forward:remote=preview", "overlay", "forward:path=/path"),
            navigator.calls,
        )
    }
}

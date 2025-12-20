package org.igorv8836.bdui.platform.ios

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.window.ComposeUIViewController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.igorv8836.bdui.actions.ActionRegistry
import org.igorv8836.bdui.actions.Router
import org.igorv8836.bdui.runtime.ScreenController
import org.igorv8836.bdui.contract.Route
import org.igorv8836.bdui.renderer.ScreenHost
import org.igorv8836.bdui.runtime.ScreenState

fun ScreenViewController(
    state: StateFlow<ScreenState> = MutableStateFlow(ScreenState()),
    router: Router = NoopRouter,
    actionRegistry: ActionRegistry = ActionRegistry(emptyMap()),
    resolve: (String) -> String = { it },
    analytics: (String, Map<String, String>) -> Unit = { _, _ -> },
    controller: ScreenController? = null,
) = ComposeUIViewController {
    controller?.onOpen()
    val flow = controller?.state ?: state
    val uiState by flow.collectAsState()
    ScreenHost(
        state = uiState,
        router = router,
        actionRegistry = actionRegistry,
        resolve = resolve,
        variableStore = controller?.variableStore,
        screenId = controller?.state?.value?.screen?.id,
        analytics = analytics,
        onRefresh = { controller?.refresh() },
        onLoadNextPage = { controller?.loadNextPage() },
        onAppear = { controller?.onAppear() },
        onFullyVisible = { controller?.onFullyVisible() },
        onDisappear = { controller?.onDisappear() },
    )
}

private object NoopRouter : Router {
    override fun navigate(route: Route) {
        // no-op default router for iOS host
    }
}

package org.igorv8836.bdui.platform.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.igorv8836.bdui.actions.ActionRegistry
import org.igorv8836.bdui.actions.Router
import org.igorv8836.bdui.contract.Route
import org.igorv8836.bdui.renderer.ScreenHost
import org.igorv8836.bdui.runtime.ScreenState

class ScreenActivity : ComponentActivity() {

    var state: StateFlow<ScreenState> = MutableStateFlow(ScreenState())
    var router: Router = NoopRouter
    var actionRegistry: ActionRegistry = ActionRegistry(emptyMap())
    var resolve: (String) -> String = { it }
    var analytics: (String, Map<String, String>) -> Unit = { _, _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val uiState by state.collectAsState()
            ScreenHost(
                state = uiState,
                router = router,
                actionRegistry = actionRegistry,
                resolve = resolve,
                analytics = analytics,
            )
        }
    }
}

private object NoopRouter : Router {
    override fun navigate(route: Route) {
        // default router does nothing; host should provide a real implementation.
    }
}

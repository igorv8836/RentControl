package org.igorv8836.bdui.renderer.host

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import org.igorv8836.bdui.contract.RemoteScreen
import org.igorv8836.bdui.core.actions.ActionRegistry
import org.igorv8836.bdui.core.context.ActionContext
import org.igorv8836.bdui.core.context.ScreenContext
import org.igorv8836.bdui.core.navigation.Navigator
import org.igorv8836.bdui.core.variables.VariableStore
import org.igorv8836.bdui.renderer.placeholders.Loading
import org.igorv8836.bdui.renderer.placeholders.Placeholder
import org.igorv8836.bdui.renderer.screen.RenderScreen
import org.igorv8836.bdui.runtime.ScreenState
import org.igorv8836.bdui.runtime.ScreenStatus

@Composable
fun ScreenHost(
    state: ScreenState,
    actionRegistry: ActionRegistry,
    variableStore: VariableStore,
    navigator: Navigator,
    screenId: String? = null,
    modifier: Modifier = Modifier,
    onRefresh: (() -> Unit)? = null,
    onLoadNextPage: (() -> Unit)? = null,
    onAppear: (() -> Unit)? = null,
    onFullyVisible: (() -> Unit)? = null,
    onDisappear: (() -> Unit)? = null,
) {
    val scope = rememberCoroutineScope()
    val variables = remember(variableStore) { variableStore }
    val variablesVersion by variables.changes.collectAsState()
    val screenContext = remember(variables, actionRegistry) {
        ScreenContext(variableStore = variables, actionRegistry = actionRegistry)
    }
    val appeared = remember { mutableStateOf(false) }
    val fullyVisible = remember { mutableStateOf(false) }

    val dispatch = { actionId: String, remoteScreen: RemoteScreen ->
        val action = remoteScreen.actions.firstOrNull { it.id == actionId }
        if (action != null) {
            scope.launch {
                actionRegistry.dispatch(
                    action = action,
                    context = ActionContext(
                        navigator = navigator,
                        screenContext = screenContext,
                        screenId = screenId ?: remoteScreen.id,
                    ),
                )
            }
        }
    }

    when (state.status) {
        ScreenStatus.Idle -> Placeholder(text = "Waiting for request...", modifier = modifier)
        ScreenStatus.Loading -> Loading(modifier = modifier)
        ScreenStatus.Error -> Placeholder(
            text = state.error ?: "Unknown error",
            modifier = modifier
        )
        ScreenStatus.Ready -> {
            val screen = state.remoteScreen ?: return
            if (state.empty) {
                Placeholder(text = "Nothing to show", modifier = modifier)
                return
            }
            RenderScreen(
                remoteScreen = screen,
                onAction = { actionId -> dispatch(actionId, screen) },
                variables = variables,
                screenId = screenId ?: screen.id,
                modifier = modifier,
                state = state,
                onRefresh = onRefresh,
                onLoadNextPage = onLoadNextPage,
                variablesVersion = variablesVersion,
            )
        }
    }

    LaunchedEffect(state.status) {
        if (state.status == ScreenStatus.Ready && !appeared.value) {
            appeared.value = true
            onAppear?.invoke()
            withFrameNanos { _ -> }
            if (!fullyVisible.value) {
                fullyVisible.value = true
                onFullyVisible?.invoke()
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            onDisappear?.invoke()
        }
    }
}

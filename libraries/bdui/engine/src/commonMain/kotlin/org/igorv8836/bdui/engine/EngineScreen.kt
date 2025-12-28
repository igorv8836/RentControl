package org.igorv8836.bdui.engine

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import org.igorv8836.bdui.renderer.ScreenHost

/**
 * Turnkey composable that wires [ScreenEngine] to [ScreenHost].
 * If you already have an engine provider, this is the only API you need on the UI side.
 */
@Composable
fun BduiScreen(
    screenId: String,
    provider: EngineProvider,
    modifier: Modifier = Modifier,
) {
    val engine = androidx.compose.runtime.remember(screenId, provider) {
        provider.engine(screenId = screenId)
    }
    LaunchedEffect(engine, screenId) {
        engine.onOpen()
    }
    val state by engine.state.collectAsState()

    ScreenHost(
        state = state,
        actionRegistry = engine.actionRegistry,
        variableStore = engine.variableStore,
        navigator = engine.navigator,
        screenId = state.remoteScreen?.id ?: screenId,
        onRefresh = { engine.refresh() },
        onLoadNextPage = { engine.loadNextPage() },
        onAppear = { engine.onAppear() },
        onFullyVisible = { engine.onFullyVisible() },
        onDisappear = { engine.onDisappear() },
        modifier = modifier,
    )
}

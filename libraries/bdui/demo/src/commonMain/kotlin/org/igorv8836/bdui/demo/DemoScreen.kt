package org.igorv8836.bdui.demo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope
import org.igorv8836.bdui.cache.config.CachePolicy
import org.igorv8836.bdui.network.BduiClient
import org.igorv8836.bdui.network.BduiClientConfig
import org.igorv8836.bdui.renderer.ScreenHost

@Composable
fun DemoScreen(
    modifier: Modifier = Modifier,
    baseUrl: String = defaultBaseUrl(),
) {
    val scope = rememberCoroutineScope()
    val client = rememberClient(scope, baseUrl)
    val state by client.store.state.collectAsState()

    LaunchedEffect(baseUrl) {
        client.load("/home")
    }

    ScreenHost(
        state = state,
        router = client.router,
        actionRegistry = client.actionRegistry,
        resolve = { it },
        variableStore = client.variables,
        screenId = state.remoteScreen?.id,
        analytics = client.config.analytics,
        onRefresh = { client.refresh() },
        onLoadNextPage = {
            val pagination = state.remoteScreen?.settings?.pagination
            client.loadNextPage(settings = pagination)
        },
        modifier = modifier,
    )
}

@Composable
private fun rememberClient(
    scope: CoroutineScope,
    baseUrl: String,
): BduiClient {
    return remember(baseUrl) {
        BduiClient(
            scope = scope,
            config = BduiClientConfig(
                baseUrl = baseUrl.trimEnd('/'),
                cachePolicy = CachePolicy(enabled = true, ttlMillis = 60_000),
            ),
        )
    }
}

// Emulator-friendly defaults; override in your app/module if needed.
private fun defaultBaseUrl(): String = "http://10.0.2.2:8080/bdui"

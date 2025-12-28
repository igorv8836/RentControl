package org.igorv8836.bdui.demo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope
import org.igorv8836.bdui.engine.EngineConfig
import org.igorv8836.bdui.engine.EngineProvider
import org.igorv8836.bdui.engine.BduiScreen

@Composable
fun DemoScreen(
    modifier: Modifier = Modifier,
    baseUrl: String = defaultBaseUrl(),
) {
    val scope = rememberCoroutineScope()
    val provider = rememberEngineProvider(scope, baseUrl)
    BduiScreen(
        screenId = "home",
        provider = provider,
        resolve = { it },
        modifier = modifier,
    )
}

@Composable
private fun rememberEngineProvider(
    scope: CoroutineScope,
    baseUrl: String,
): EngineProvider {
    return remember(baseUrl) {
        EngineProvider(
            scope = scope,
            config = EngineConfig(
                baseUrl = baseUrl.trimEnd('/'),
            ),
        )
    }
}

// Emulator-friendly defaults; override in your app/module if needed.
private fun defaultBaseUrl(): String = "http://10.0.2.2:8080/bdui"

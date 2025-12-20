package org.igorv8836.bdui.renderer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.igorv8836.bdui.actions.ActionContext
import org.igorv8836.bdui.actions.ActionRegistry
import org.igorv8836.bdui.actions.Router
import org.igorv8836.bdui.components.ButtonComponent
import org.igorv8836.bdui.components.ContainerComponent
import org.igorv8836.bdui.components.ImagePlaceholder
import org.igorv8836.bdui.components.LazyListComponent
import org.igorv8836.bdui.components.TextComponent
import org.igorv8836.bdui.contract.ButtonElement
import org.igorv8836.bdui.contract.ComponentNode
import org.igorv8836.bdui.contract.Container
import org.igorv8836.bdui.contract.ImageElement
import org.igorv8836.bdui.contract.LazyListElement
import org.igorv8836.bdui.contract.Screen
import org.igorv8836.bdui.contract.TextElement
import org.igorv8836.bdui.runtime.ScreenState
import org.igorv8836.bdui.runtime.ScreenStatus

@Composable
fun ScreenHost(
    state: ScreenState,
    router: Router,
    actionRegistry: ActionRegistry,
    resolve: (String) -> String,
    modifier: Modifier = Modifier,
    analytics: (String, Map<String, String>) -> Unit = { _, _ -> },
) {
    val scope = rememberCoroutineScope()
    val dispatch = { actionId: String, screen: Screen ->
        val action = screen.actions.firstOrNull { it.id == actionId }
        if (action != null) {
            scope.launch {
                actionRegistry.dispatch(
                    action = action,
                    context = ActionContext(router = router, analytics = analytics),
                )
            }
        }
    }

    when (state.status) {
        ScreenStatus.Idle -> Placeholder(text = "Waiting for request...", modifier = modifier)
        ScreenStatus.Loading -> Loading(modifier = modifier)
        ScreenStatus.Error -> Placeholder(text = state.error ?: "Unknown error", modifier = modifier)
        ScreenStatus.Ready -> {
            val screen = state.screen ?: return
            RenderScreen(
                screen = screen,
                onAction = { actionId -> dispatch(actionId, screen) },
                resolve = resolve,
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun RenderScreen(
    screen: Screen,
    onAction: (String) -> Unit,
    resolve: (String) -> String,
    modifier: Modifier = Modifier,
) {
    val root = screen.layout.root
    val hasLazyList = containsLazyList(root)
    val contentModifier = Modifier
        .fillMaxSize()
        .padding(16.dp)

    if (hasLazyList) {
        Box(modifier = modifier.then(contentModifier)) {
            RenderNode(
                node = root,
                onAction = onAction,
                resolve = resolve,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    } else {
        val scrollState = rememberScrollState()
        Column(
            modifier = modifier.then(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
            ),
        ) {
            RenderNode(
                node = root,
                onAction = onAction,
                resolve = resolve,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

private fun containsLazyList(node: ComponentNode): Boolean =
    when (node) {
        is LazyListElement -> true
        is Container -> node.children.any { child -> containsLazyList(child) }
        else -> false
    }

@Composable
private fun RenderNode(
    node: ComponentNode,
    onAction: (String) -> Unit,
    resolve: (String) -> String,
    modifier: Modifier = Modifier,
) {
    when (node) {
        is TextElement -> TextComponent(node = node, resolve = resolve, modifier = modifier)
        is ButtonElement -> ButtonComponent(
            node = node,
            resolve = resolve,
            onAction = onAction,
            modifier = modifier,
        )

        is ImageElement -> ImagePlaceholder(node = node, modifier = modifier)
        is Container -> ContainerComponent(
            node = node,
            renderChild = { child, childModifier ->
                RenderNode(
                    node = child,
                    onAction = onAction,
                    resolve = resolve,
                    modifier = childModifier,
                )
            },
            modifier = modifier,
        )

        is LazyListElement -> LazyListComponent(
            node = node,
            renderChild = { child, childModifier ->
                RenderNode(
                    node = child,
                    onAction = onAction,
                    resolve = resolve,
                    modifier = childModifier,
                )
            },
            modifier = modifier,
        )
    }
}

@Composable
private fun Loading(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        CircularProgressIndicator(modifier = Modifier.padding(16.dp))
    }
}

@Composable
private fun Placeholder(
    text: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(16.dp)) {
        Text(text = text, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@file:OptIn(ExperimentalMaterialApi::class)

package org.igorv8836.bdui.renderer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.igorv8836.bdui.actions.ActionContext
import org.igorv8836.bdui.actions.ActionRegistry
import org.igorv8836.bdui.actions.Router
import org.igorv8836.bdui.components.ButtonComponent
import org.igorv8836.bdui.components.ContainerComponent
import org.igorv8836.bdui.components.DividerComponent
import org.igorv8836.bdui.components.ImagePlaceholder
import org.igorv8836.bdui.components.ListItemComponent
import org.igorv8836.bdui.components.LazyListComponent
import org.igorv8836.bdui.components.SpacerComponent
import org.igorv8836.bdui.components.TextComponent
import org.igorv8836.bdui.contract.ButtonElement
import org.igorv8836.bdui.contract.ComponentNode
import org.igorv8836.bdui.contract.Container
import org.igorv8836.bdui.contract.DividerElement
import org.igorv8836.bdui.contract.ImageElement
import org.igorv8836.bdui.contract.LazyListElement
import org.igorv8836.bdui.contract.ListItemElement
import org.igorv8836.bdui.contract.ScreenSettings
import org.igorv8836.bdui.contract.SpacerElement
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
    onRefresh: (() -> Unit)? = null,
    onLoadNextPage: (() -> Unit)? = null,
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
            if (state.empty) {
                Placeholder(text = "Nothing to show", modifier = modifier)
                return
            }
            RenderScreen(
                screen = screen,
                onAction = { actionId -> dispatch(actionId, screen) },
                resolve = resolve,
                modifier = modifier,
                state = state,
                onRefresh = onRefresh,
                onLoadNextPage = onLoadNextPage,
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
    state: ScreenState,
    onRefresh: (() -> Unit)?,
    onLoadNextPage: (() -> Unit)?,
) {
    val root = screen.layout.root
    val hasLazyList = containsLazyList(root)
    val contentModifier = Modifier
        .fillMaxSize()
        .padding(16.dp)

    val settings = screen.settings
    val isScrollable = settings.scrollable && !hasLazyList
    val paginationConfig = settings.pagination?.takeIf { it.enabled }?.let {
        PaginationConfig(
            prefetchDistance = it.prefetchDistance,
            onLoadNextPage = onLoadNextPage,
            loadingMore = state.loadingMore,
        )
    }
    val pullRefreshEnabled = settings.pullToRefresh?.enabled == true && onRefresh != null

    val pullRefreshState = if (pullRefreshEnabled) {
        rememberPullRefreshState(refreshing = state.refreshing, onRefresh = { onRefresh?.invoke() })
    } else null

    val baseModifier = if (pullRefreshEnabled) {
        modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState!!)
    } else modifier.fillMaxSize()

    val content: @Composable BoxScope.() -> Unit = {
        if (hasLazyList) {
            Box(modifier = contentModifier) {
                RenderNode(
                    node = root,
                    onAction = onAction,
                    resolve = resolve,
                    modifier = Modifier.fillMaxWidth(),
                    pagination = paginationConfig,
                )
            }
        } else {
            val scrollState = rememberScrollState()
            val scrollModifier = if (isScrollable) {
                Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp)
            } else {
                Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            }
            Column(
                modifier = scrollModifier,
            ) {
                RenderNode(
                    node = root,
                    onAction = onAction,
                    resolve = resolve,
                    modifier = Modifier.fillMaxWidth(),
                    pagination = paginationConfig,
                )
            }
        }

        if (pullRefreshEnabled && pullRefreshState != null) {
            PullRefreshIndicator(
                refreshing = state.refreshing,
                state = pullRefreshState,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .align(Alignment.TopCenter),
            )
        }
    }

    Box(modifier = baseModifier) {
        content()
    }
}

private fun containsLazyList(node: ComponentNode): Boolean =
    when (node) {
        is LazyListElement -> true
        is Container -> node.children.any { child -> containsLazyList(child) }
        else -> false
    }

private data class PaginationConfig(
    val prefetchDistance: Int,
    val onLoadNextPage: (() -> Unit)?,
    val loadingMore: Boolean,
)

@Composable
private fun RenderNode(
    node: ComponentNode,
    onAction: (String) -> Unit,
    resolve: (String) -> String,
    modifier: Modifier = Modifier,
    pagination: PaginationConfig? = null,
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
                    pagination = pagination,
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
                    pagination = pagination,
                )
            },
            modifier = modifier,
            onLoadNextPage = pagination?.onLoadNextPage,
            prefetchDistance = pagination?.prefetchDistance ?: 2,
            loadingMore = pagination?.loadingMore ?: false,
        )
        is SpacerElement -> SpacerComponent(node = node, modifier = modifier)
        is DividerElement -> DividerComponent(node = node, modifier = modifier)
        is ListItemElement -> ListItemComponent(
            node = node,
            resolve = resolve,
            onAction = { id -> onAction(id) },
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

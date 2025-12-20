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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
import org.igorv8836.bdui.runtime.VariableStore
import org.igorv8836.bdui.contract.Binding
import org.igorv8836.bdui.contract.Condition
import org.igorv8836.bdui.contract.MissingVariableBehavior
import org.igorv8836.bdui.contract.VariableScope
import org.igorv8836.bdui.contract.VariableValue

@Composable
fun ScreenHost(
    state: ScreenState,
    router: Router,
    actionRegistry: ActionRegistry,
    resolve: (String) -> String,
    variableStore: VariableStore? = null,
    screenId: String? = null,
    modifier: Modifier = Modifier,
    analytics: (String, Map<String, String>) -> Unit = { _, _ -> },
    onRefresh: (() -> Unit)? = null,
    onLoadNextPage: (() -> Unit)? = null,
    onAppear: (() -> Unit)? = null,
    onFullyVisible: (() -> Unit)? = null,
    onDisappear: (() -> Unit)? = null,
) {
    val scope = rememberCoroutineScope()
    val variables = remember(variableStore) { variableStore ?: VariableStore(scope = scope) }
    val variablesVersion by variables.changes.collectAsState()
    val appeared = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    val fullyVisible = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

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
            androidx.compose.runtime.withFrameNanos { _ -> }
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

@Composable
private fun RenderScreen(
    screen: Screen,
    onAction: (String) -> Unit,
    resolve: (String) -> String,
    variables: VariableStore,
    screenId: String,
    variablesVersion: Long,
    modifier: Modifier = Modifier,
    state: ScreenState,
    onRefresh: (() -> Unit)?,
    onLoadNextPage: (() -> Unit)?,
) {
    val root = screen.layout.root
    val hasLazyList = containsLazyList(root)
    val resolver = remember(screen.id, variablesVersion) {
        BindingResolver(
            variables = variables,
            screenId = screenId,
            translate = resolve,
        )
    }
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
                    resolver = resolver,
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
                    resolver = resolver,
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

private class BindingResolver(
    private val variables: VariableStore,
    private val screenId: String,
    private val translate: (String) -> String,
) {
    fun text(key: String?, binding: Binding?, template: String?): String {
        val direct = binding?.let { resolveBinding(it) }
        if (direct != null) return direct
        val base = template ?: key?.takeIf { it.isNotBlank() }?.let(translate)
        return base?.let { interpolate(it) } ?: ""
    }

    fun isVisible(condition: Condition?): Boolean {
        if (condition == null) return true
        val value = resolveValue(condition.binding)
        val exists = value != null
        if (!exists) {
            return !condition.exists
        }
        var result = condition.equals?.let { equalsValue(value!!, it) } ?: isTruthy(value!!)
        if (condition.negate) result = !result
        return result
    }

    fun isEnabled(base: Boolean, condition: Condition?): Boolean {
        if (!base) return false
        return isVisible(condition)
    }

    private fun resolveBinding(binding: Binding): String? {
        val value = resolveValue(binding)
        return value?.let { valueToString(it) }
    }

    private fun resolveValue(binding: Binding): VariableValue? {
        val stored = variables.peek(binding.key, binding.scope, screenId)
        return when {
            stored != null -> stored
            binding.missingBehavior == MissingVariableBehavior.Default -> binding.default
            binding.missingBehavior == MissingVariableBehavior.Error -> throw IllegalStateException("Variable '${binding.key}' is missing")
            else -> null
        }
    }

    private fun interpolate(template: String): String {
        val regex = Regex("\\{\\{\\s*(.*?)\\s*\\}\\}")
        return regex.replace(template) { match ->
            val key = match.groupValues.getOrNull(1)?.trim().orEmpty()
            val resolved = variables.peek(key, VariableScope.Global, screenId)
            resolved?.let { valueToString(it) } ?: ""
        }
    }

    private fun isTruthy(value: VariableValue): Boolean =
        when (value) {
            is VariableValue.BoolValue -> value.value
            is VariableValue.NumberValue -> value.value != 0.0
            is VariableValue.StringValue -> value.value.isNotEmpty()
            is VariableValue.ObjectValue -> value.value.isNotEmpty()
        }

    private fun equalsValue(left: VariableValue, right: VariableValue): Boolean =
        when {
            left is VariableValue.StringValue && right is VariableValue.StringValue -> left.value == right.value
            left is VariableValue.NumberValue && right is VariableValue.NumberValue -> left.value == right.value
            left is VariableValue.BoolValue && right is VariableValue.BoolValue -> left.value == right.value
            left is VariableValue.ObjectValue && right is VariableValue.ObjectValue -> left.value == right.value
            else -> false
        }

    private fun valueToString(value: VariableValue): String =
        when (value) {
            is VariableValue.StringValue -> value.value
            is VariableValue.NumberValue -> value.value.toString()
            is VariableValue.BoolValue -> value.value.toString()
            is VariableValue.ObjectValue -> value.value.entries.joinToString(
                separator = ", ",
                prefix = "{",
                postfix = "}",
            ) { (key, inner) -> "$key:${valueToString(inner)}" }
        }
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
    resolver: BindingResolver,
    modifier: Modifier = Modifier,
    pagination: PaginationConfig? = null,
) {
    when (node) {
        is TextElement -> {
            if (!resolver.isVisible(node.visibleIf)) return
            val text = resolver.text(node.textKey, node.binding, node.template)
            TextComponent(node = node, text = text, modifier = modifier)
        }
        is ButtonElement -> ButtonComponent(
            node = node,
            title = resolver.text(node.titleKey, node.titleBinding, null),
            enabled = resolver.isEnabled(node.isEnabled, node.enabledIf),
            onAction = onAction,
            modifier = modifier,
        )

        is ImageElement -> if (resolver.isVisible(node.visibleIf)) {
            ImagePlaceholder(node = node, modifier = modifier)
        }
        is Container -> if (resolver.isVisible(node.visibleIf)) {
            ContainerComponent(
                node = node,
                renderChild = { child, childModifier ->
                    RenderNode(
                        node = child,
                        onAction = onAction,
                        resolver = resolver,
                        modifier = childModifier,
                        pagination = pagination,
                    )
                },
                modifier = modifier,
            )
        }

        is LazyListElement -> if (resolver.isVisible(node.visibleIf)) {
            LazyListComponent(
                node = node,
                renderChild = { child, childModifier ->
                    RenderNode(
                        node = child,
                        onAction = onAction,
                        resolver = resolver,
                        modifier = childModifier,
                        pagination = pagination,
                    )
                },
                modifier = modifier,
                onLoadNextPage = pagination?.onLoadNextPage,
                prefetchDistance = pagination?.prefetchDistance ?: 2,
                loadingMore = pagination?.loadingMore ?: false,
            )
        }
        is SpacerElement -> if (resolver.isVisible(node.visibleIf)) {
            SpacerComponent(node = node, modifier = modifier)
        }
        is DividerElement -> if (resolver.isVisible(node.visibleIf)) {
            DividerComponent(node = node, modifier = modifier)
        }
        is ListItemElement -> if (resolver.isVisible(node.visibleIf)) {
            ListItemComponent(
                node = node,
                title = resolver.text(node.titleKey, node.titleBinding, null),
                subtitle = node.subtitleKey?.let { key -> resolver.text(key, node.subtitleBinding, null) },
                onAction = { id -> onAction(id) },
                enabled = resolver.isEnabled(true, node.enabledIf),
                modifier = modifier,
            )
        }
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

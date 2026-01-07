package org.igorv8836.bdui.renderer.screen

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import org.igorv8836.bdui.contract.ComponentNode
import org.igorv8836.bdui.contract.Container
import org.igorv8836.bdui.contract.LazyListElement
import org.igorv8836.bdui.contract.RemoteScreen
import org.igorv8836.bdui.contract.Section
import org.igorv8836.bdui.core.variables.VariableStore
import org.igorv8836.bdui.renderer.binding.BindingResolver
import org.igorv8836.bdui.renderer.navigation.BottomNavigationBar
import org.igorv8836.bdui.renderer.node.PaginationConfig
import org.igorv8836.bdui.renderer.node.RenderNode
import org.igorv8836.bdui.renderer.section.RenderSections
import org.igorv8836.bdui.runtime.ScreenState

@Composable
@OptIn(ExperimentalMaterialApi::class)
internal fun RenderScreen(
    remoteScreen: RemoteScreen,
    onAction: (String) -> Unit,
    variables: VariableStore,
    screenId: String,
    variablesVersion: Long,
    modifier: Modifier = Modifier,
    state: ScreenState,
    onRefresh: (() -> Unit)?,
    onLoadNextPage: (() -> Unit)?,
) {
    val root = remoteScreen.layout.root
    val sections = remoteScreen.layout.sections
    val scaffold = remoteScreen.layout.scaffold
    val bottomBar = scaffold?.bottomBar
    val hasLazyList = root?.let { containsLazyList(it) } == true
    val resolver = remember(remoteScreen.id, variablesVersion) {
        BindingResolver(
            variables = variables,
            screenId = screenId,
        )
    }
    val contentModifier = Modifier
        .fillMaxSize()
        .padding(16.dp)

    val settings = remoteScreen.settings
    val isScrollable = settings.scrollable && !hasLazyList && sections.isEmpty()
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
        if (sections.isNotEmpty()) {
            RenderSections(
                sections = sections,
                resolver = resolver,
                pagination = paginationConfig,
                onAction = onAction,
            )
        } else if (hasLazyList) {
            Column(
                modifier = contentModifier.fillMaxSize(),
            ) {
                scaffold?.top?.let {
                    RenderNode(
                        node = it,
                        onAction = onAction,
                        resolver = resolver,
                        modifier = Modifier.fillMaxWidth(),
                        pagination = paginationConfig,
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f, fill = true)
                        .fillMaxWidth(),
                ) {
                    RenderNode(
                        node = root!!,
                        onAction = onAction,
                        resolver = resolver,
                        modifier = Modifier.fillMaxWidth(),
                        pagination = paginationConfig,
                    )
                }
                scaffold?.bottom?.let {
                    RenderNode(
                        node = it,
                        onAction = onAction,
                        resolver = resolver,
                        modifier = Modifier.fillMaxWidth(),
                        pagination = paginationConfig,
                    )
                }
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
                scaffold?.top?.let {
                    RenderNode(
                        node = it,
                        onAction = onAction,
                        resolver = resolver,
                        modifier = Modifier.fillMaxWidth(),
                        pagination = paginationConfig,
                    )
                }
                root?.let {
                    RenderNode(
                        node = it,
                        onAction = onAction,
                        resolver = resolver,
                        modifier = Modifier.fillMaxWidth(),
                        pagination = paginationConfig,
                    )
                }
                scaffold?.bottom?.let {
                    RenderNode(
                        node = it,
                        onAction = onAction,
                        resolver = resolver,
                        modifier = Modifier.fillMaxWidth(),
                        pagination = paginationConfig,
                    )
                }
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

    val bottomBarContent: (@Composable () -> Unit)? = bottomBar?.let { bar ->
        val visibleTabs = bar.tabs.filter { tab -> resolver.isVisible(tab.visibleIf) }
        var selectedTabId by remember(bar.tabs.map { it.id }, variablesVersion) {
            mutableStateOf(bar.selectedTabId ?: visibleTabs.firstOrNull()?.id)
        }
        if (selectedTabId !in visibleTabs.map { it.id }) {
            selectedTabId = visibleTabs.firstOrNull()?.id
        }

        {
            BottomNavigationBar(
                bottomBar = bar,
                resolver = resolver,
                selectedTabId = selectedTabId,
                onTabSelected = { tab ->
                    selectedTabId = tab.id
                    onAction(tab.actionId)
                },
            )
        }
    }

    if (bottomBarContent != null) {
        Column(modifier = baseModifier) {
            Box(modifier = Modifier.weight(1f)) {
                content()
            }
            bottomBarContent()
        }
    } else {
        Box(modifier = baseModifier) {
            content()
        }
    }
}

private fun containsLazyList(node: ComponentNode): Boolean =
    when (node) {
        is LazyListElement -> true
        is Container -> node.children.any { child -> containsLazyList(child) }
        else -> false
    }

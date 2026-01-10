package org.igorv8836.bdui.renderer.section

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.igorv8836.bdui.contract.ComponentNode
import org.igorv8836.bdui.contract.Container
import org.igorv8836.bdui.contract.LazyListElement
import org.igorv8836.bdui.contract.ScrollOrientation
import org.igorv8836.bdui.contract.Section
import org.igorv8836.bdui.contract.StickyEdge
import org.igorv8836.bdui.contract.StickyMode
import org.igorv8836.bdui.renderer.binding.BindingResolver
import org.igorv8836.bdui.renderer.node.PaginationConfig
import org.igorv8836.bdui.renderer.node.RenderNode

private enum class ScrollDirection {
    Up,
    Down,
}

@Composable
internal fun RenderSections(
    sections: List<Section>,
    resolver: BindingResolver,
    pagination: PaginationConfig?,
    onAction: (String) -> Unit,
) {
    val spacing = 16.dp
    val listState = rememberLazyListState()
    val visibleSections = sections.filter { resolver.isVisible(it.visibleIf) }

    var lastIndex by remember { mutableIntStateOf(0) }
    var lastOffset by remember { mutableIntStateOf(0) }
    var direction by remember { mutableStateOf(ScrollDirection.Down) }

    LaunchedEffect(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) {
        val newIndex = listState.firstVisibleItemIndex
        val newOffset = listState.firstVisibleItemScrollOffset
        direction = when {
            newIndex > lastIndex -> ScrollDirection.Down
            newIndex < lastIndex -> ScrollDirection.Up
            newOffset > lastOffset -> ScrollDirection.Down
            newOffset < lastOffset -> ScrollDirection.Up
            else -> direction
        }
        lastIndex = newIndex
        lastOffset = newOffset
    }

    val firstVisibleIndex = listState.firstVisibleItemIndex
    val pinnedTop = remember(visibleSections, firstVisibleIndex) {
        visibleSections
            .withIndex()
            .filter { (index, section) -> section.sticky?.edge == StickyEdge.Top && index < firstVisibleIndex }
            .maxByOrNull { it.index }
            ?.value
    }
    val pinnedBottom = remember(visibleSections, firstVisibleIndex) {
        visibleSections
            .withIndex()
            .filter { (index, section) -> section.sticky?.edge == StickyEdge.Bottom && index < firstVisibleIndex }
            .maxByOrNull { it.index }
            ?.value
    }

    val pinnedTopSticky = pinnedTop?.sticky
    val pinnedBottomSticky = pinnedBottom?.sticky

    val showPinnedTop = pinnedTopSticky != null && when (pinnedTopSticky.mode) {
        StickyMode.Always -> true
        StickyMode.OnScrollTowardsEdge -> direction == ScrollDirection.Up
    }
    val showPinnedBottom = pinnedBottomSticky != null && when (pinnedBottomSticky.mode) {
        StickyMode.Always -> true
        StickyMode.OnScrollTowardsEdge -> direction == ScrollDirection.Down
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(spacing),
        ) {
            visibleSections.forEach { section ->
                item(key = section.id) {
                    SectionContent(section, resolver, pagination, onAction, inLazyList = true)
                }
            }
        }

        if (showPinnedTop) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter),
            ) {
                SectionContent(pinnedTop!!, resolver, pagination, onAction, inLazyList = false)
            }
        }

        if (showPinnedBottom) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
            ) {
                SectionContent(pinnedBottom!!, resolver, pagination, onAction, inLazyList = false)
            }
        }
    }
}

@Composable
private fun SectionContent(
    section: Section,
    resolver: BindingResolver,
    pagination: PaginationConfig?,
    onAction: (String) -> Unit,
    inLazyList: Boolean,
) {
    val padding = section.scroll.contentPadding?.dp ?: 0.dp
    val isLazyList = containsLazyList(section.content)
    val content: @Composable () -> Unit = {
        RenderNode(
            node = section.content,
            onAction = onAction,
            resolver = resolver,
            modifier = Modifier.fillMaxWidth(),
            pagination = pagination,
        )
    }

    val enableScroll = section.scroll.enabled &&
        !(isLazyList && section.scroll.orientation == ScrollOrientation.Vertical) &&
        !inLazyList

    if (enableScroll) {
        when (section.scroll.orientation) {
            ScrollOrientation.Vertical -> {
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(padding)
                        .verticalScroll(scrollState, enabled = section.scroll.userScrollEnabled),
                ) {
                    content()
                }
            }

            ScrollOrientation.Horizontal -> {
                val scrollState = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(padding)
                        .horizontalScroll(scrollState, enabled = section.scroll.userScrollEnabled),
                ) {
                    content()
                }
            }
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding),
        ) {
            content()
        }
    }
}

private fun containsLazyList(node: ComponentNode): Boolean =
    when (node) {
        is LazyListElement -> true
        is Container -> node.children.any(::containsLazyList)
        else -> false
    }

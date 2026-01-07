package org.igorv8836.bdui.renderer.section

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.igorv8836.bdui.contract.ScrollOrientation
import org.igorv8836.bdui.contract.Section
import org.igorv8836.bdui.contract.SectionSticky
import org.igorv8836.bdui.renderer.binding.BindingResolver
import org.igorv8836.bdui.renderer.node.PaginationConfig
import org.igorv8836.bdui.renderer.node.RenderNode

@Composable
internal fun RenderSections(
    sections: List<Section>,
    resolver: BindingResolver,
    pagination: PaginationConfig?,
    onAction: (String) -> Unit,
) {
    val spacing = 16.dp
    val stickyTop = sections.filter { it.sticky == SectionSticky.Top && resolver.isVisible(it.visibleIf) }
    val stickyBottom = sections.filter { it.sticky == SectionSticky.Bottom && resolver.isVisible(it.visibleIf) }
    val main = sections.filter { it.sticky == SectionSticky.None && resolver.isVisible(it.visibleIf) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (stickyTop.isNotEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    stickyTop.forEach { section ->
                        SectionContent(section, resolver, pagination, onAction, inLazyList = false)
                        Spacer(modifier = Modifier.height(spacing))
                    }
                }
            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = true),
                verticalArrangement = Arrangement.spacedBy(spacing),
            ) {
                items(main, key = { it.id }) { section ->
                    SectionContent(section, resolver, pagination, onAction, inLazyList = true)
                }
            }
        }

        if (stickyBottom.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = spacing, start = spacing, end = spacing),
                verticalArrangement = Arrangement.spacedBy(spacing),
            ) {
                stickyBottom.forEach { section ->
                    SectionContent(section, resolver, pagination, onAction, inLazyList = false)
                }
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
    val isLazyList = section.content is org.igorv8836.bdui.contract.LazyListElement
    val content: @Composable () -> Unit = {
        RenderNode(
            node = section.content,
            onAction = onAction,
            resolver = resolver,
            modifier = Modifier.fillMaxWidth(),
            pagination = pagination,
        )
    }
    val enableScroll = section.scroll.enabled && !(isLazyList && section.scroll.orientation == ScrollOrientation.Vertical) && !inLazyList
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

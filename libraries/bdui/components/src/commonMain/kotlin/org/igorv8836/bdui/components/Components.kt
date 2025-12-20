package org.igorv8836.bdui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.igorv8836.bdui.contract.ButtonElement
import org.igorv8836.bdui.contract.ComponentNode
import org.igorv8836.bdui.contract.Container
import org.igorv8836.bdui.contract.DividerElement
import org.igorv8836.bdui.contract.ImageElement
import org.igorv8836.bdui.contract.LazyListElement
import org.igorv8836.bdui.contract.ListItemElement
import org.igorv8836.bdui.contract.SpacerElement
import org.igorv8836.bdui.contract.TextElement
import org.igorv8836.bdui.contract.ContainerDirection

@Composable
fun TextComponent(
    node: TextElement,
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(text = text, modifier = modifier)
}

@Composable
fun ButtonComponent(
    node: ButtonElement,
    title: String,
    enabled: Boolean,
    onAction: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = { if (enabled) onAction(node.actionId) },
        enabled = enabled,
        modifier = modifier,
    ) {
        Text(text = title)
    }
}

@Composable
fun ImagePlaceholder(
    node: ImageElement,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Text(
            text = node.description.orEmpty(),
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun ContainerComponent(
    node: Container,
    renderChild: @Composable (ComponentNode, Modifier) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = node.spacing?.dp ?: 8.dp
    val hasLazyChild = node.children.any { it is LazyListElement }

    when (node.direction) {
        ContainerDirection.Column -> Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(spacing),
        ) {
            node.children.forEach { child ->
                val childModifier =
                    if (hasLazyChild && child is LazyListElement) Modifier.weight(1f) else Modifier
                renderChild(child, childModifier)
            }
        }

        ContainerDirection.Row -> Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(spacing),
        ) {
            node.children.forEach { child ->
                renderChild(child, Modifier)
            }
        }

        ContainerDirection.Overlay -> Box(modifier = modifier) {
            node.children.forEach { child ->
                renderChild(child, Modifier)
            }
        }
    }
}

@Composable
fun LazyListComponent(
    node: LazyListElement,
    renderChild: @Composable (ComponentNode, Modifier) -> Unit,
    modifier: Modifier = Modifier,
    contentSpacing: Dp = 12.dp,
    onLoadNextPage: (() -> Unit)? = null,
    prefetchDistance: Int = 2,
    loadingMore: Boolean = false,
) {
    val listState = rememberLazyListState()
    var requestInFlight by remember { mutableStateOf(false) }

    LaunchedEffect(loadingMore) {
        if (!loadingMore) requestInFlight = false
    }

    LaunchedEffect(listState, onLoadNextPage, prefetchDistance, loadingMore) {
        if (onLoadNextPage == null) return@LaunchedEffect
        snapshotFlow<Pair<Int, Int>> {
            val info = listState.layoutInfo
            val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            Pair(lastVisible, info.totalItemsCount)
        }.collect { (lastVisible, total) ->
            if (total == 0) return@collect
            if (lastVisible >= total - 1 - prefetchDistance && !requestInFlight && !loadingMore) {
                requestInFlight = true
                onLoadNextPage()
            }
        }
    }

    LazyColumn(
        modifier = modifier,
        state = listState,
        verticalArrangement = Arrangement.spacedBy(contentSpacing),
    ) {
        items(node.items, key = { it.id }) { child ->
            renderChild(child, Modifier.fillMaxWidth())
        }
        if (node.items.isEmpty() && node.placeholderCount > 0) {
            items(node.placeholderCount) { index ->
                PlaceholderRow(placeholderId = "${node.id}-$index")
            }
        }
    }
}

@Composable
fun SpacerComponent(
    node: SpacerElement,
    modifier: Modifier = Modifier,
) {
    val height = node.height?.dp ?: 0.dp
    val width = node.width?.dp
    val applied = if (width != null) {
        modifier
            .height(height)
            .fillMaxWidth()
    } else {
        modifier.height(height)
    }
    Spacer(modifier = applied)
}

@Composable
fun DividerComponent(
    node: DividerElement,
    modifier: Modifier = Modifier,
) {
    Divider(
        modifier = modifier,
        thickness = (node.thickness ?: 1f).dp,
    )
}

@Composable
fun ListItemComponent(
    node: ListItemElement,
    title: String,
    subtitle: String?,
    onAction: ((String) -> Unit)?,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .let { it },
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
        )
        subtitle?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        val actionId = node.actionId
        if (actionId != null && onAction != null && enabled) {
            Button(onClick = { onAction(actionId) }) {
                Text(text = title)
            }
        }
    }
}

@Composable
private fun PlaceholderRow(
    placeholderId: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(Color.LightGray.copy(alpha = 0.2f)),
    ) {
        Text(
            text = placeholderId,
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

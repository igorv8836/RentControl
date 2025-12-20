package org.igorv8836.bdui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.igorv8836.bdui.contract.ButtonElement
import org.igorv8836.bdui.contract.ButtonKind
import org.igorv8836.bdui.contract.ComponentNode
import org.igorv8836.bdui.contract.Container
import org.igorv8836.bdui.contract.ImageElement
import org.igorv8836.bdui.contract.LazyListElement
import org.igorv8836.bdui.contract.TextElement
import org.igorv8836.bdui.contract.ContainerDirection

@Composable
fun TextComponent(
    node: TextElement,
    resolve: (String) -> String,
    modifier: Modifier = Modifier,
) {
    Text(text = resolve(node.textKey), modifier = modifier)
}

@Composable
fun ButtonComponent(
    node: ButtonElement,
    resolve: (String) -> String,
    onAction: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = { if (node.isEnabled) onAction(node.actionId) },
        enabled = node.isEnabled,
        modifier = modifier,
    ) {
        Text(text = resolve(node.titleKey))
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
) {
    LazyColumn(
        modifier = modifier,
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

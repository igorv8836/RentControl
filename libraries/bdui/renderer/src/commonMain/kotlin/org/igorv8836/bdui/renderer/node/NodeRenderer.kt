package org.igorv8836.bdui.renderer.node

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.igorv8836.bdui.components.ButtonComponent
import org.igorv8836.bdui.components.ContainerComponent
import org.igorv8836.bdui.components.DividerComponent
import org.igorv8836.bdui.components.ImagePlaceholder
import org.igorv8836.bdui.components.LazyListComponent
import org.igorv8836.bdui.components.ListItemComponent
import org.igorv8836.bdui.components.SpacerComponent
import org.igorv8836.bdui.components.TextComponent
import org.igorv8836.bdui.contract.ButtonElement
import org.igorv8836.bdui.contract.ComponentNode
import org.igorv8836.bdui.contract.Container
import org.igorv8836.bdui.contract.DividerElement
import org.igorv8836.bdui.contract.ImageElement
import org.igorv8836.bdui.contract.LazyListElement
import org.igorv8836.bdui.contract.ListItemElement
import org.igorv8836.bdui.contract.SpacerElement
import org.igorv8836.bdui.contract.TextElement
import org.igorv8836.bdui.renderer.binding.BindingResolver

internal data class PaginationConfig(
    val prefetchDistance: Int,
    val onLoadNextPage: (() -> Unit)?,
    val loadingMore: Boolean,
)

@Composable
internal fun RenderNode(
    node: ComponentNode,
    onAction: (String) -> Unit,
    resolver: BindingResolver,
    modifier: Modifier = Modifier,
    pagination: PaginationConfig? = null,
) {
    when (node) {
        is TextElement -> {
            if (!resolver.isVisible(node.visibleIf)) return
            val text = resolver.text(node.text, node.template)
            TextComponent(node = node, text = text, modifier = modifier)
        }
        is ButtonElement -> ButtonComponent(
            node = node,
            title = resolver.text(node.title, null),
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
                title = resolver.text(node.title, null),
                subtitle = node.subtitle?.let { key -> resolver.text(key, null) },
                onAction = { id -> onAction(id) },
                enabled = resolver.isEnabled(true, node.enabledIf),
                modifier = modifier,
            )
        }
    }
}

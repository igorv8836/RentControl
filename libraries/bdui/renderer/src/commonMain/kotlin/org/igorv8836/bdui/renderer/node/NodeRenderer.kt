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
import org.igorv8836.bdui.contract.CardElement
import org.igorv8836.bdui.contract.CardGridElement
import org.igorv8836.bdui.contract.TabsElement
import org.igorv8836.bdui.contract.TextFieldElement
import org.igorv8836.bdui.contract.DropdownElement
import org.igorv8836.bdui.contract.SliderElement
import org.igorv8836.bdui.contract.SwitchElement
import org.igorv8836.bdui.contract.ChipGroupElement
import org.igorv8836.bdui.contract.CarouselElement
import org.igorv8836.bdui.contract.ModalElement
import org.igorv8836.bdui.contract.SnackbarElement
import org.igorv8836.bdui.contract.StateElement
import org.igorv8836.bdui.contract.ProgressElement
import org.igorv8836.bdui.contract.MapElement
import org.igorv8836.bdui.renderer.binding.BindingResolver
import org.igorv8836.bdui.components.CardComponent
import org.igorv8836.bdui.components.CardGridComponent
import org.igorv8836.bdui.components.TabsComponent
import org.igorv8836.bdui.components.TextFieldComponent
import org.igorv8836.bdui.components.DropdownComponent
import org.igorv8836.bdui.components.SliderComponent
import org.igorv8836.bdui.components.SwitchComponent
import org.igorv8836.bdui.components.ChipGroupComponent
import org.igorv8836.bdui.components.CarouselComponent
import org.igorv8836.bdui.components.SnackbarComponent
import org.igorv8836.bdui.components.StateComponent
import org.igorv8836.bdui.components.ProgressComponent
import org.igorv8836.bdui.components.MapPlaceholderComponent

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
        is CardGridElement -> if (resolver.isVisible(node.visibleIf)) {
            CardGridComponent(
                node = node,
                renderCard = { card ->
                    if (resolver.isVisible(card.visibleIf)) {
                        CardComponent(
                            node = card,
                            onAction = card.actionId?.let { id -> { onAction(id) } },
                        )
                    }
                },
                modifier = modifier,
            )
        }
        is CardElement -> if (resolver.isVisible(node.visibleIf)) {
            CardComponent(
                node = node,
                modifier = modifier,
                onAction = { actionId -> onAction(actionId) },
            )
        }
        is TabsElement -> if (resolver.isVisible(node.visibleIf)) {
            val visibleTabs = node.tabs.filter { tab -> resolver.isVisible(tab.visibleIf) }
            TabsComponent(
                node = node.copy(tabs = visibleTabs),
                modifier = modifier,
                onAction = onAction,
            )
        }
        is TextFieldElement -> if (resolver.isVisible(node.visibleIf)) {
            TextFieldComponent(
                node = node,
                modifier = modifier,
                onAction = { id -> id?.let(onAction) },
            )
        }
        is DropdownElement -> if (resolver.isVisible(node.visibleIf)) {
            DropdownComponent(
                node = node,
                modifier = modifier,
                onAction = { id -> id?.let(onAction) },
            )
        }
        is SliderElement -> if (resolver.isVisible(node.visibleIf)) {
            SliderComponent(
                node = node,
                modifier = modifier,
                onAction = { id -> id?.let(onAction) },
            )
        }
        is SwitchElement -> if (resolver.isVisible(node.visibleIf)) {
            SwitchComponent(
                node = node,
                modifier = modifier,
                onAction = { id -> id?.let(onAction) },
            )
        }
        is ChipGroupElement -> if (resolver.isVisible(node.visibleIf)) {
            ChipGroupComponent(
                node = node,
                modifier = modifier,
                onAction = { actionId -> actionId?.let(onAction) },
            )
        }
        is CarouselElement -> if (resolver.isVisible(node.visibleIf)) {
            CarouselComponent(
                node = node,
                renderChild = { child ->
                    RenderNode(
                        node = child,
                        onAction = onAction,
                        resolver = resolver,
                        modifier = Modifier,
                        pagination = pagination,
                    )
                },
                modifier = modifier,
            )
        }
        is ModalElement -> if (resolver.isVisible(node.visibleIf)) {
            CardComponent(
                node = CardElement(
                    id = node.id,
                    title = "",
                    subtitle = null,
                    actionId = node.primaryActionId,
                ),
                modifier = modifier,
                onAction = { id -> onAction(id) },
            )
            RenderNode(
                node = node.content,
                onAction = onAction,
                resolver = resolver,
                modifier = modifier,
                pagination = pagination,
            )
        }
        is SnackbarElement -> if (resolver.isVisible(node.visibleIf)) {
            SnackbarComponent(
                node = node,
                modifier = modifier,
                onAction = { id -> id?.let(onAction) },
            )
        }
        is StateElement -> if (resolver.isVisible(node.visibleIf)) {
            StateComponent(
                node = node,
                modifier = modifier,
                onAction = { id -> id?.let(onAction) },
            )
        }
        is ProgressElement -> if (resolver.isVisible(node.visibleIf)) {
            ProgressComponent(
                node = node,
                modifier = modifier,
            )
        }
        is MapElement -> if (resolver.isVisible(node.visibleIf)) {
            MapPlaceholderComponent(
                node = node,
                modifier = modifier,
            )
        }
    }
}

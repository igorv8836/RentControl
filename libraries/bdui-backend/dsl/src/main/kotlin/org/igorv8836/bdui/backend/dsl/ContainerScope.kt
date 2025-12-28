package org.igorv8836.bdui.backend.dsl

import org.igorv8836.bdui.contract.ButtonElement
import org.igorv8836.bdui.contract.ButtonKind
import org.igorv8836.bdui.contract.ComponentNode
import org.igorv8836.bdui.contract.Condition
import org.igorv8836.bdui.contract.Container
import org.igorv8836.bdui.contract.ContainerDirection
import org.igorv8836.bdui.contract.DividerElement
import org.igorv8836.bdui.contract.LazyListElement
import org.igorv8836.bdui.contract.ListItemElement
import org.igorv8836.bdui.contract.Semantics
import org.igorv8836.bdui.contract.SpacerElement
import org.igorv8836.bdui.contract.TextElement
import org.igorv8836.bdui.contract.TextStyle
import org.igorv8836.bdui.contract.BottomBar
import org.igorv8836.bdui.contract.BottomTab

/**
 * Container builder scope for nested layout declarations.
 */
class ContainerScope {
    internal val children = mutableListOf<ComponentNode>()

    fun container(
        id: String,
        direction: ContainerDirection = ContainerDirection.Column,
        spacing: Float? = null,
        visibleIf: Condition? = null,
        block: ContainerScope.() -> Unit = {},
    ): Container {
        val scope = ContainerScope().apply(block)
        return Container(
            id = id,
            direction = direction,
            spacing = spacing,
            children = scope.children.toList(),
            visibleIf = visibleIf,
        ).also { children += it }
    }

    fun text(
        id: String,
        text: String,
        style: TextStyle = TextStyle.Body,
        template: String? = null,
        semantics: Semantics? = null,
        visibleIf: Condition? = null,
    ): TextElement = TextElement(
        id = id,
        text = text,
        style = style,
        semantics = semantics,
        template = template,
        visibleIf = visibleIf,
    ).also { children += it }

    fun button(
        id: String,
        title: String,
        actionId: String,
        kind: ButtonKind = ButtonKind.Primary,
        isEnabled: Boolean = true,
        enabledIf: Condition? = null,
        visibleIf: Condition? = null,
        semantics: Semantics? = null,
    ): ButtonElement = ButtonElement(
        id = id,
        title = title,
        actionId = actionId,
        kind = kind,
        isEnabled = isEnabled,
        semantics = semantics,
        enabledIf = enabledIf,
        visibleIf = visibleIf,
    ).also { children += it }

    fun spacer(
        id: String,
        width: Float? = null,
        height: Float? = null,
        visibleIf: Condition? = null,
    ): SpacerElement = SpacerElement(
        id = id,
        width = width,
        height = height,
        visibleIf = visibleIf,
    ).also { children += it }

    fun divider(
        id: String,
        thickness: Float? = null,
        color: String? = null,
        insetStart: Float? = null,
        visibleIf: Condition? = null,
    ): DividerElement = DividerElement(
        id = id,
        thickness = thickness,
        color = color,
        insetStart = insetStart,
        visibleIf = visibleIf,
    ).also { children += it }

    fun list(
        id: String,
        items: List<ComponentNode>,
        placeholderCount: Int = 0,
        visibleIf: Condition? = null,
    ): LazyListElement = LazyListElement(
        id = id,
        items = items,
        placeholderCount = placeholderCount,
        visibleIf = visibleIf,
    ).also { children += it }

    fun list(
        id: String,
        placeholderCount: Int = 0,
        visibleIf: Condition? = null,
        block: ContainerScope.() -> Unit,
    ): LazyListElement {
        val scope = ContainerScope().apply(block)
        return list(
            id = id,
            items = scope.children.toList(),
            placeholderCount = placeholderCount,
            visibleIf = visibleIf,
        )
    }

    fun listItem(
        id: String,
        title: String,
        subtitle: String? = null,
        actionId: String? = null,
        semantics: Semantics? = null,
        enabledIf: Condition? = null,
        visibleIf: Condition? = null,
    ): ListItemElement = ListItemElement(
        id = id,
        title = title,
        subtitle = subtitle,
        actionId = actionId,
        semantics = semantics,
        enabledIf = enabledIf,
        visibleIf = visibleIf,
    ).also { children += it }

    fun bottomBar(selectedTabId: String? = null, block: BottomBarBuilder.() -> Unit): BottomBar {
        val builder = BottomBarBuilder(selectedTabId)
        builder.block()
        return builder.build()
    }
}

class BottomBarBuilder(private val selected: String?) {
    private val tabs = mutableListOf<BottomTab>()

    fun tab(
        id: String,
        title: String,
        actionId: String,
        iconUrl: String? = null,
        badge: String? = null,
        visibleIf: org.igorv8836.bdui.contract.Condition? = null,
    ) {
        tabs += BottomTab(
            id = id,
            title = title,
            actionId = actionId,
            iconUrl = iconUrl,
            badge = badge,
            visibleIf = visibleIf,
        )
    }

    fun build(): BottomBar = BottomBar(
        tabs = tabs.toList(),
        selectedTabId = selected,
    )
}

/**
 * Helper to quickly build a container tree as root.
 */
fun container(
    id: String,
    direction: ContainerDirection = ContainerDirection.Column,
    spacing: Float? = null,
    visibleIf: Condition? = null,
    block: ContainerScope.() -> Unit = {},
): Container {
    val scope = ContainerScope().apply(block)
    return Container(
        id = id,
        direction = direction,
        spacing = spacing,
        visibleIf = visibleIf,
        children = scope.children.toList(),
    )
}

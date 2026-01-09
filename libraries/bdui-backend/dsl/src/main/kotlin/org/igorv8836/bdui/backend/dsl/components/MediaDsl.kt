package org.igorv8836.bdui.backend.dsl

import org.igorv8836.bdui.contract.Action
import org.igorv8836.bdui.contract.CarouselElement
import org.igorv8836.bdui.contract.Color
import org.igorv8836.bdui.contract.ComponentNode
import org.igorv8836.bdui.contract.Condition
import org.igorv8836.bdui.contract.ImageElement
import org.igorv8836.bdui.contract.MapElement
import org.igorv8836.bdui.contract.ModalElement

fun ContainerScope.image(
    id: String,
    url: String,
    description: String? = null,
    backgroundColor: Color? = null,
    textColor: Color? = null,
    visibleIf: Condition? = null,
): ImageElement = ImageElement(
    id = id,
    url = url,
    description = description,
    backgroundColor = backgroundColor,
    textColor = textColor,
    visibleIf = visibleIf,
).also { children += it }

fun ContainerScope.carousel(
    id: String,
    backgroundColor: Color? = null,
    visibleIf: Condition? = null,
    block: ContainerScope.() -> Unit,
): CarouselElement {
    val scope = ContainerScope(ctx).apply(block)
    return CarouselElement(
        id = id,
        items = scope.children.toList(),
        backgroundColor = backgroundColor,
        visibleIf = visibleIf,
    ).also { children += it }
}

fun ContainerScope.modal(
    id: String,
    content: ComponentNode,
    primaryAction: Action? = null,
    dismissAction: Action? = null,
    backgroundColor: Color? = null,
    scrimColor: Color? = null,
    visibleIf: Condition? = null,
): ModalElement {
    val primaryActionId = primaryAction?.id
    val dismissActionId = dismissAction?.id
    primaryAction?.let { ctx.register(it) }
    dismissAction?.let { ctx.register(it) }

    return ModalElement(
        id = id,
        content = content,
        primaryActionId = primaryActionId,
        dismissActionId = dismissActionId,
        backgroundColor = backgroundColor,
        scrimColor = scrimColor,
        visibleIf = visibleIf,
    ).also { children += it }
}

fun ContainerScope.map(
    id: String,
    title: String? = null,
    subtitle: String? = null,
    titleColor: Color? = null,
    subtitleColor: Color? = null,
    backgroundColor: Color? = null,
    visibleIf: Condition? = null,
): MapElement = MapElement(
    id = id,
    title = title,
    subtitle = subtitle,
    titleColor = titleColor,
    subtitleColor = subtitleColor,
    backgroundColor = backgroundColor,
    visibleIf = visibleIf,
).also { children += it }


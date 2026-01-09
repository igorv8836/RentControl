package org.igorv8836.bdui.backend.dsl

import org.igorv8836.bdui.backend.core.RenderContext
import org.igorv8836.bdui.contract.Action
import org.igorv8836.bdui.contract.CardElement
import org.igorv8836.bdui.contract.CardGridElement
import org.igorv8836.bdui.contract.Color
import org.igorv8836.bdui.contract.Condition

class CardGridScope internal constructor(
    private val ctx: RenderContext,
) {
    internal val items = mutableListOf<CardElement>()

    fun card(
        id: String,
        title: String,
        subtitle: String? = null,
        imageUrl: String? = null,
        badge: String? = null,
        action: Action? = null,
        titleColor: Color? = null,
        subtitleColor: Color? = null,
        badgeTextColor: Color? = null,
        badgeBackgroundColor: Color? = null,
        backgroundColor: Color? = null,
        visibleIf: Condition? = null,
    ): CardElement {
        val actionId = action?.id
        action?.let { ctx.register(it) }

        return CardElement(
            id = id,
            title = title,
            subtitle = subtitle,
            imageUrl = imageUrl,
            badge = badge,
            actionId = actionId,
            titleColor = titleColor,
            subtitleColor = subtitleColor,
            badgeTextColor = badgeTextColor,
            badgeBackgroundColor = badgeBackgroundColor,
            backgroundColor = backgroundColor,
            visibleIf = visibleIf,
        ).also { items += it }
    }
}

fun ContainerScope.card(
    id: String,
    title: String,
    subtitle: String? = null,
    imageUrl: String? = null,
    badge: String? = null,
    action: Action? = null,
    titleColor: Color? = null,
    subtitleColor: Color? = null,
    badgeTextColor: Color? = null,
    badgeBackgroundColor: Color? = null,
    backgroundColor: Color? = null,
    visibleIf: Condition? = null,
): CardElement {
    val actionId = action?.id
    action?.let { ctx.register(it) }

    return CardElement(
        id = id,
        title = title,
        subtitle = subtitle,
        imageUrl = imageUrl,
        badge = badge,
        actionId = actionId,
        titleColor = titleColor,
        subtitleColor = subtitleColor,
        badgeTextColor = badgeTextColor,
        badgeBackgroundColor = badgeBackgroundColor,
        backgroundColor = backgroundColor,
        visibleIf = visibleIf,
    ).also { children += it }
}

fun ContainerScope.cardGrid(
    id: String,
    columns: Int = 2,
    backgroundColor: Color? = null,
    visibleIf: Condition? = null,
    block: CardGridScope.() -> Unit,
): CardGridElement {
    val scope = CardGridScope(ctx).apply(block)
    return CardGridElement(
        id = id,
        items = scope.items.toList(),
        columns = columns,
        backgroundColor = backgroundColor,
        visibleIf = visibleIf,
    ).also { children += it }
}


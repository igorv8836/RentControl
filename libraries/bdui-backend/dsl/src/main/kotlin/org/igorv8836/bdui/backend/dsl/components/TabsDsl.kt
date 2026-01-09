package org.igorv8836.bdui.backend.dsl

import org.igorv8836.bdui.backend.core.RenderContext
import org.igorv8836.bdui.contract.Action
import org.igorv8836.bdui.contract.Color
import org.igorv8836.bdui.contract.Condition
import org.igorv8836.bdui.contract.TabItem
import org.igorv8836.bdui.contract.TabsElement

class TabsScope internal constructor(
    private val ctx: RenderContext,
) {
    internal val tabs = mutableListOf<TabItem>()

    fun tab(
        id: String,
        title: String,
        action: Action,
        badge: String? = null,
        textColor: Color? = null,
        selectedTextColor: Color? = null,
        backgroundColor: Color? = null,
        selectedBackgroundColor: Color? = null,
        badgeTextColor: Color? = null,
        badgeBackgroundColor: Color? = null,
        visibleIf: Condition? = null,
    ) {
        ctx.register(action)
        tabs += TabItem(
            id = id,
            title = title,
            actionId = action.id,
            badge = badge,
            textColor = textColor,
            selectedTextColor = selectedTextColor,
            backgroundColor = backgroundColor,
            selectedBackgroundColor = selectedBackgroundColor,
            badgeTextColor = badgeTextColor,
            badgeBackgroundColor = badgeBackgroundColor,
            visibleIf = visibleIf,
        )
    }
}

fun ContainerScope.tabs(
    id: String,
    selectedTabId: String? = null,
    selectedTabTextColor: Color? = null,
    unselectedTabTextColor: Color? = null,
    selectedTabBackgroundColor: Color? = null,
    unselectedTabBackgroundColor: Color? = null,
    visibleIf: Condition? = null,
    block: TabsScope.() -> Unit,
): TabsElement {
    val scope = TabsScope(ctx).apply(block)
    return TabsElement(
        id = id,
        tabs = scope.tabs.toList(),
        selectedTabId = selectedTabId,
        selectedTabTextColor = selectedTabTextColor,
        unselectedTabTextColor = unselectedTabTextColor,
        selectedTabBackgroundColor = selectedTabBackgroundColor,
        unselectedTabBackgroundColor = unselectedTabBackgroundColor,
        visibleIf = visibleIf,
    ).also { children += it }
}


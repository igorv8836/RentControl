package org.igorv8836.rentcontrol.backend.v2

import org.igorv8836.bdui.backend.dsl.container
import org.igorv8836.bdui.backend.dsl.incrementVariableAction
import org.igorv8836.bdui.backend.core.RenderContext
import org.igorv8836.bdui.backend.dsl.color
import org.igorv8836.bdui.backend.runtime.engine.Renderer
import org.igorv8836.bdui.contract.ButtonKind
import org.igorv8836.bdui.contract.ComponentNode
import org.igorv8836.bdui.contract.ContainerDirection
import org.igorv8836.bdui.contract.ForwardAction
import org.igorv8836.bdui.contract.Scaffold
import org.igorv8836.bdui.contract.VariableScope

class HomeHeaderRenderer : Renderer<HomeHeaderData, ComponentNode> {
    override fun render(data: HomeHeaderData, ctx: RenderContext): ComponentNode = container(
        id = "header",
        direction = ContainerDirection.Column,
        spacing = 8f,
        ctx = ctx,
    ) {
        val goCatalog = ForwardAction(id = "go-catalog", path = "catalog")
        text(
            id = "welcome-title",
            text = "welcome",
            template = "Welcome, ${data.userName}!",
        )
        text(
            id = "visits",
            text = "visits",
            template = "Visits: @{visits}",
        )
        button(
            id = "cta-catalog",
            title = "Open catalog",
            action = goCatalog,
            kind = ButtonKind.Primary,
            textColor = color(
                light = "#FFFF00",
                dark = "#000000",
            ),
        )
    }
}

class HomeOffersRenderer : Renderer<HomeOffersData, ComponentNode> {
    override fun render(data: HomeOffersData, ctx: RenderContext): ComponentNode = container(
        id = "offers-wrapper",
        direction = ContainerDirection.Column,
        spacing = 16f,
        ctx = ctx,
    ) {
        divider(id = "middle-divider")
        text(
            id = "offers-title",
            text = "offers",
            template = "Featured offers",
        )
        list(
            id = "offers-list",
            placeholderCount = 0,
            ctx = ctx,
        ) {
            data.offers.forEach { offer ->
                val openDetails = ForwardAction(
                    id = "open-${offer.id}",
                    path = "details/${offer.id}",
                )
                listItem(
                    id = "offer-${offer.id}",
                    title = offer.title,
                    subtitle = offer.subtitle,
                    action = openDetails,
                )
            }
        }
    }
}

class HomeFooterRenderer : Renderer<HomeFooterData, ComponentNode> {
    override fun render(data: HomeFooterData, ctx: RenderContext): ComponentNode = container(
        id = "footer",
        direction = ContainerDirection.Row,
        spacing = 12f,
        ctx = ctx,
    ) {
        val setUser = org.igorv8836.bdui.backend.dsl.setVariableAction(
            id = data.refreshActionId,
            key = "user_name",
            value = org.igorv8836.bdui.contract.StringValue("Guest"),
            scope = VariableScope.Global,
        )
        val incVisits = incrementVariableAction(
            id = data.incVisitsActionId,
            key = "visits",
            delta = 1.0,
            scope = VariableScope.Global,
        )
        button(
            id = "btn-refresh-user",
            title = "Refresh user",
            action = setUser,
            kind = ButtonKind.Secondary,
        )
        button(
            id = "btn-inc-visits",
            title = "Add visit",
            action = incVisits,
            kind = ButtonKind.Primary,
        )
    }
}

class HomeScaffoldRenderer : Renderer<HomeScaffoldData, Scaffold> {
    override fun render(data: HomeScaffoldData, ctx: RenderContext): Scaffold {
        val goHome = ForwardAction(id = "go-home", path = "home")
        val goCatalog = ForwardAction(id = "go-catalog", path = "catalog")
        val barWithActions = data.bottomBar.copy(
            tabs = data.bottomBar.tabs.map { tab ->
                val action = when (tab.id) {
                    "home-tab" -> goHome
                    "catalog-tab" -> goCatalog
                    else -> null
                }
                action?.let { ctx.register(it) }
                tab.copy(actionId = action?.id ?: tab.actionId)
            },
        )
        return Scaffold(
            top = null,
            bottom = null,
            bottomBar = barWithActions,
        )
    }
}

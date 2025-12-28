package org.igorv8836.rentcontrol.backend.screen

import org.igorv8836.bdui.backend.dsl.container
import org.igorv8836.bdui.backend.dsl.incrementVariableAction
import org.igorv8836.bdui.backend.dsl.screen
import org.igorv8836.bdui.backend.dsl.setVariableAction
import org.igorv8836.bdui.backend.dsl.variableChangedTrigger
import org.igorv8836.bdui.contract.Action
import org.igorv8836.bdui.contract.ButtonKind
import org.igorv8836.bdui.contract.Condition
import org.igorv8836.bdui.contract.ContainerDirection
import org.igorv8836.bdui.contract.ForwardAction
import org.igorv8836.bdui.contract.NumberValue
import org.igorv8836.bdui.contract.PaginationSettings
import org.igorv8836.bdui.contract.PullToRefresh
import org.igorv8836.bdui.contract.RemoteScreen
import org.igorv8836.bdui.contract.StringValue
import org.igorv8836.bdui.contract.VariableScope
import org.igorv8836.rentcontrol.backend.model.OfferDto

fun buildHomeScreen(offers: List<OfferDto>): RemoteScreen = screen(id = "home", version = 1) {
    settings(
        scrollable = true,
        pullToRefresh = PullToRefresh(enabled = true),
        pagination = PaginationSettings(enabled = false),
    )

    val setUser = setVariableAction(
        id = "set-user",
        key = "user_name",
        value = StringValue("Guest"),
        scope = VariableScope.Global,
    )
    val initVisits = setVariableAction(
        id = "init-visits",
        key = "visits",
        value = NumberValue(0.0),
        scope = VariableScope.Global,
    )
    val incVisits = incrementVariableAction(
        id = "inc-visits",
        key = "visits",
        delta = 1.0,
        scope = VariableScope.Global,
    )
    val triggerWelcome = variableChangedTrigger(
        id = "welcome-trigger",
        key = "user_name",
        actions = emptyList<Action>(),
        condition = Condition(
            key = "user_name",
            equals = StringValue("Guest"),
        ),
    )

    val goHome = ForwardAction(id = "go-home", path = "home")
    val goCatalog = ForwardAction(id = "go-catalog", path = "catalog")
    val actions = buildList<Action> {
        add(setUser)
        add(initVisits)
        add(incVisits)
        add(goHome)
        add(goCatalog)
        offers.forEach { offer ->
            add(ForwardAction(id = "open-${offer.id}", path = "details/${offer.id}"))
        }
    }

    actions(*actions.toTypedArray())
    trigger(triggerWelcome)

    // Build scaffold content outside root to avoid duplicate ids
    val header = container(
        id = "header",
        direction = ContainerDirection.Column,
        spacing = 8f,
    ) {
        text(
            id = "welcome-title",
            text = "welcome",
            template = "Welcome, @{user_name}!",
        )
        text(
            id = "visits",
            text = "visits",
            template = "Visits: @{visits}",
        )
        button(
            id = "cta-catalog",
            title = "Open catalog",
            actionId = goCatalog.id,
            kind = ButtonKind.Primary,
        )
    }

    val footer = container(
        id = "footer",
        direction = ContainerDirection.Row,
        spacing = 12f,
    ) {
        button(
            id = "btn-refresh-user",
            title = "Refresh user",
            actionId = setUser.id,
            kind = ButtonKind.Secondary,
        )
        button(
            id = "btn-inc-visits",
            title = "Add visit",
            actionId = incVisits.id,
            kind = ButtonKind.Primary,
        )
    }

    val bottomBar = org.igorv8836.bdui.backend.dsl.ContainerScope().bottomBar(selectedTabId = "home-tab") {
        tab(id = "home-tab", title = "Home", actionId = goHome.id)
        tab(id = "catalog-tab", title = "Catalog", actionId = goCatalog.id)
    }

    scaffold(top = header, bottom = footer, bottomBar = bottomBar)

    layout(
        container(
            id = "root",
            direction = ContainerDirection.Column,
            spacing = 16f,
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
            ) {
                offers.forEach { offer ->
                    listItem(
                        id = "offer-${offer.id}",
                        title = offer.title,
                        subtitle = offer.subtitle,
                        actionId = "open-${offer.id}",
                    )
                }
            }
        },
    )
}

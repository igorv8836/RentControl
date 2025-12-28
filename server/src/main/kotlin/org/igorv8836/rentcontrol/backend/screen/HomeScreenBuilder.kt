package org.igorv8836.rentcontrol.backend.screen

import org.igorv8836.bdui.backend.dsl.bindText
import org.igorv8836.bdui.backend.dsl.container
import org.igorv8836.bdui.backend.dsl.screen
import org.igorv8836.bdui.backend.dsl.setVariableAction
import org.igorv8836.bdui.backend.dsl.variableChangedTrigger
import org.igorv8836.bdui.contract.Action
import org.igorv8836.bdui.contract.ButtonKind
import org.igorv8836.bdui.contract.Condition
import org.igorv8836.bdui.contract.ContainerDirection
import org.igorv8836.bdui.contract.ForwardAction
import org.igorv8836.bdui.contract.MissingVariableBehavior
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
    val incVisits = setVariableAction(
        id = "inc-visits",
        key = "visits",
        value = NumberValue(1.0),
        scope = VariableScope.Global,
    )
    val triggerWelcome = variableChangedTrigger(
        id = "welcome-trigger",
        key = "user_name",
        actions = emptyList<Action>(),
        condition = Condition(
            binding = bindText("user_name", missingBehavior = MissingVariableBehavior.Empty),
            equals = StringValue(value = "Guest"),
        ),
    )

    val goCatalog = ForwardAction(id = "go-catalog", path = "catalog")
    val actions = buildList<Action> {
        add(setUser)
        add(incVisits)
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
            textKey = "welcome",
            template = "Welcome, {{user_name}}!",
            binding = bindText(
                "user_name",
                missingBehavior = MissingVariableBehavior.Default,
                default = StringValue("Guest"),
            ),
        )
        text(
            id = "visits",
            textKey = "visits",
            template = "Visits: {{visits}}",
            binding = bindText(
                "visits",
                missingBehavior = MissingVariableBehavior.Default,
                default = NumberValue(0.0),
            ),
        )
        button(
            id = "cta-catalog",
            titleKey = "Open catalog",
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
            titleKey = "Refresh user",
            actionId = setUser.id,
            kind = ButtonKind.Secondary,
        )
        button(
            id = "btn-inc-visits",
            titleKey = "Add visit",
            actionId = incVisits.id,
            kind = ButtonKind.Primary,
        )
    }

    scaffold(top = header, bottom = footer)

    layout(
        container(
            id = "root",
            direction = ContainerDirection.Column,
            spacing = 16f,
        ) {
            divider(id = "middle-divider")
            text(
                id = "offers-title",
                textKey = "offers",
                template = "Featured offers",
            )
            list(
                id = "offers-list",
                placeholderCount = 0,
            ) {
                offers.forEach { offer ->
                    listItem(
                        id = "offer-${offer.id}",
                        titleKey = offer.title,
                        subtitleKey = offer.subtitle,
                        actionId = "open-${offer.id}",
                    )
                }
            }
        },
    )
}

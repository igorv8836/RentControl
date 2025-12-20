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
import org.igorv8836.bdui.contract.MissingVariableBehavior
import org.igorv8836.bdui.contract.PaginationSettings
import org.igorv8836.bdui.contract.PullToRefresh
import org.igorv8836.bdui.contract.VariableScope
import org.igorv8836.bdui.contract.VariableValue
import org.igorv8836.bdui.contract.RemoteScreen
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
        value = VariableValue.StringValue("Guest"),
        scope = VariableScope.Global,
    )
    val incVisits = setVariableAction(
        id = "inc-visits",
        key = "visits",
        value = VariableValue.NumberValue(1.0),
        scope = VariableScope.Global,
    )
    val triggerWelcome = variableChangedTrigger(
        id = "welcome-trigger",
        key = "user_name",
        actions = emptyList<Action>(),
        condition = Condition(
            binding = bindText("user_name", missingBehavior = MissingVariableBehavior.Empty),
            equals = VariableValue.StringValue("Guest"),
        ),
    )

    actions(setUser, incVisits)
    trigger(triggerWelcome)

    layout(
        container(
            id = "root",
            direction = ContainerDirection.Column,
            spacing = 16f,
        ) {
            text(
                id = "welcome-title",
                textKey = "welcome",
                template = "Welcome, {{user_name}}!",
                binding = bindText("user_name", missingBehavior = MissingVariableBehavior.Default),
            )
            text(
                id = "visits",
                textKey = "visits",
                template = "Visits: {{visits}}",
                binding = bindText("visits", missingBehavior = MissingVariableBehavior.Default),
            )
            divider(id = "top-divider")
            container(
                id = "cta-row",
                direction = ContainerDirection.Row,
                spacing = 12f,
            ) {
                button(
                    id = "btn-refresh-user",
                    titleKey = "refresh_user",
                    actionId = setUser.id,
                    kind = ButtonKind.Secondary,
                )
                button(
                    id = "btn-inc-visits",
                    titleKey = "add_visit",
                    actionId = incVisits.id,
                    kind = ButtonKind.Primary,
                )
            }
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
                        actionId = null,
                    )
                }
            }
        },
    )
}

package org.igorv8836.rentcontrol.backend.screen

import org.igorv8836.bdui.backend.dsl.container
import org.igorv8836.bdui.backend.dsl.screen
import org.igorv8836.bdui.contract.ButtonKind
import org.igorv8836.bdui.contract.ContainerDirection
import org.igorv8836.bdui.contract.ForwardAction
import org.igorv8836.bdui.contract.PaginationSettings
import org.igorv8836.bdui.contract.PullToRefresh
import org.igorv8836.bdui.contract.RemoteScreen
import org.igorv8836.rentcontrol.backend.model.OfferDto

fun buildHomeScreen(offers: List<OfferDto>): RemoteScreen = screen(id = "home", version = 1) {
    val toCatalog = ForwardAction(id = "go-catalog", path = "catalog")
    val toDetails = offers.map { offer ->
        ForwardAction(id = "open-${offer.id}", path = "details/${offer.id}")
    }

    actions(toCatalog, *toDetails.toTypedArray())

    settings(
        scrollable = true,
        pullToRefresh = PullToRefresh(enabled = true),
        pagination = PaginationSettings(enabled = false),
    )

    layout(
        container(
            id = "root",
            direction = ContainerDirection.Column,
            spacing = 16f,
        ) {
            text(
                id = "welcome-title",
                textKey = "Backend-driven UI playground",
            )
            text(
                id = "welcome-sub",
                textKey = "Tap any card to open details or use the catalog.",
            )
            button(
                id = "cta-catalog",
                titleKey = "Open catalog",
                actionId = toCatalog.id,
                kind = ButtonKind.Primary,
            )
            text(
                id = "offers-title",
                textKey = "Featured offers",
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

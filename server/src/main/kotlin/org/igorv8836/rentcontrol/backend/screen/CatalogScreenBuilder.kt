package org.igorv8836.rentcontrol.backend.screen

import org.igorv8836.bdui.backend.dsl.container
import org.igorv8836.bdui.backend.dsl.screen
import org.igorv8836.bdui.contract.ButtonKind
import org.igorv8836.bdui.contract.ContainerDirection
import org.igorv8836.bdui.contract.ForwardAction
import org.igorv8836.bdui.contract.RemoteScreen
import org.igorv8836.rentcontrol.backend.model.OfferDto

fun buildCatalogScreen(offers: List<OfferDto>): RemoteScreen = screen(id = "catalog", version = 1) {
    val backToHome = ForwardAction(id = "back-home", path = "home")
    val toDetails = offers.map { offer ->
        ForwardAction(id = "catalog-details-${offer.id}", path = "details/${offer.id}")
    }
    actions(backToHome, *toDetails.toTypedArray())

    layout(
        container(
            id = "catalog-root",
            direction = ContainerDirection.Column,
            spacing = 12f,
        ) {
            text(id = "catalog-title", textKey = "Catalog")
            text(
                id = "catalog-subtitle",
                textKey = "Pick any item to open its details. The list is served from the backend.",
            )
            button(
                id = "catalog-home",
                titleKey = "Back to home",
                actionId = backToHome.id,
                kind = ButtonKind.Secondary,
            )
            list(id = "catalog-list", placeholderCount = 0) {
                offers.forEach { offer ->
                    listItem(
                        id = "catalog-${offer.id}",
                        titleKey = offer.title,
                        subtitleKey = offer.subtitle,
                        actionId = "catalog-details-${offer.id}",
                    )
                }
            }
        },
    )
}

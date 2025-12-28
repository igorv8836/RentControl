package org.igorv8836.rentcontrol.backend.screen

import org.igorv8836.bdui.backend.dsl.container
import org.igorv8836.bdui.backend.dsl.screen
import org.igorv8836.bdui.contract.ButtonKind
import org.igorv8836.bdui.contract.ContainerDirection
import org.igorv8836.bdui.contract.ForwardAction
import org.igorv8836.bdui.contract.RemoteScreen
import org.igorv8836.rentcontrol.backend.model.OfferDto

fun buildCatalogScreen(offers: List<OfferDto>): RemoteScreen = screen(id = "catalog", version = 1) {
    val goHome = ForwardAction(id = "go-home", path = "home")
    val goCatalog = ForwardAction(id = "go-catalog", path = "catalog")
    val toDetails = offers.map { offer ->
        ForwardAction(id = "catalog-details-${offer.id}", path = "details/${offer.id}")
    }
    actions(goHome, goCatalog, *toDetails.toTypedArray())

    layout(
        container(
            id = "catalog-root",
            direction = ContainerDirection.Column,
            spacing = 12f,
        ) {
            text(id = "catalog-title", text = "Catalog")
            text(
                id = "catalog-subtitle",
                text = "Pick any item to open its details. The list is served from the backend.",
            )
            button(
                id = "catalog-home",
                title = "Back to home",
                actionId = goHome.id,
                kind = ButtonKind.Secondary,
            )
            list(id = "catalog-list", placeholderCount = 0) {
                offers.forEach { offer ->
                    listItem(
                        id = "catalog-${offer.id}",
                        title = offer.title,
                        subtitle = offer.subtitle,
                        actionId = "catalog-details-${offer.id}",
                    )
                }
            }
        },
    )

    val bottomBar = org.igorv8836.bdui.backend.dsl.ContainerScope().bottomBar(selectedTabId = "catalog-tab") {
        tab(id = "home-tab", title = "Home", actionId = goHome.id)
        tab(id = "catalog-tab", title = "Catalog", actionId = goCatalog.id)
    }
    scaffold(bottomBar = bottomBar)
}

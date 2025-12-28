package org.igorv8836.rentcontrol.backend.screen

import org.igorv8836.bdui.backend.dsl.container
import org.igorv8836.bdui.backend.dsl.screen
import org.igorv8836.bdui.contract.ButtonKind
import org.igorv8836.bdui.contract.ContainerDirection
import org.igorv8836.bdui.contract.ForwardAction
import org.igorv8836.bdui.contract.RemoteScreen
import org.igorv8836.rentcontrol.backend.model.OfferDto

fun buildDetailsScreen(offer: OfferDto): RemoteScreen = screen(id = "details/${offer.id}", version = 1) {
    val goCatalog = ForwardAction(id = "go-catalog", path = "catalog")
    val goHome = ForwardAction(id = "go-home", path = "home")
    actions(goCatalog, goHome)

    layout(
        container(
            id = "details-root",
            direction = ContainerDirection.Column,
            spacing = 12f,
        ) {
            text(id = "details-title", text = offer.title)
            text(id = "details-subtitle", text = offer.subtitle)
            text(id = "details-description", text = offer.description)
            container(
                id = "details-actions",
                direction = ContainerDirection.Row,
                spacing = 12f,
            ) {
                button(
                    id = "details-home",
                    title = "Back to home",
                    actionId = goHome.id,
                    kind = ButtonKind.Secondary,
                )
                button(
                    id = "details-catalog",
                    title = "Open catalog",
                    actionId = goCatalog.id,
                    kind = ButtonKind.Primary,
                )
            }
        },
    )

    val bottomBar = org.igorv8836.bdui.backend.dsl.ContainerScope().bottomBar(selectedTabId = "catalog-tab") {
        tab(id = "home-tab", title = "Home", actionId = goHome.id)
        tab(id = "catalog-tab", title = "Catalog", actionId = goCatalog.id)
    }
    scaffold(bottomBar = bottomBar)
}

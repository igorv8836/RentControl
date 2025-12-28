package org.igorv8836.rentcontrol.backend.screen

import org.igorv8836.bdui.backend.dsl.container
import org.igorv8836.bdui.backend.dsl.screen
import org.igorv8836.bdui.contract.ButtonKind
import org.igorv8836.bdui.contract.ContainerDirection
import org.igorv8836.bdui.contract.ForwardAction
import org.igorv8836.bdui.contract.RemoteScreen
import org.igorv8836.rentcontrol.backend.model.OfferDto

fun buildDetailsScreen(offer: OfferDto): RemoteScreen = screen(id = "details/${offer.id}", version = 1) {
    val toCatalog = ForwardAction(id = "details-back-catalog", path = "catalog")
    val toHome = ForwardAction(id = "details-back-home", path = "home")
    actions(toCatalog, toHome)

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
                    actionId = toHome.id,
                    kind = ButtonKind.Secondary,
                )
                button(
                    id = "details-catalog",
                    title = "Open catalog",
                    actionId = toCatalog.id,
                    kind = ButtonKind.Primary,
                )
            }
        },
    )
}

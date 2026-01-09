package org.igorv8836.bdui.backend.dsl

import org.igorv8836.bdui.backend.core.RenderContext
import org.igorv8836.bdui.contract.ForwardAction
import org.igorv8836.bdui.contract.ProgressStyle
import org.igorv8836.bdui.contract.StateKind
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ColorsCoverageDslTest {

    @Test
    fun `dsl supports colors for most elements and auto registers actions`() {
        val ctx = RenderContext()
        val scope = ContainerScope(ctx)

        val goCatalog = ForwardAction(id = "go-catalog", path = "catalog")
        val openDetails = ForwardAction(id = "open-details", path = "details/1")
        val selectTab = ForwardAction(id = "select-tab", path = "home")
        val chipAction = ForwardAction(id = "chip-action", path = "chips")
        val modalPrimary = ForwardAction(id = "modal-primary", path = "ok")
        val modalDismiss = ForwardAction(id = "modal-dismiss", path = "cancel")

        val text = scope.text(
            id = "t",
            text = "Hello",
            textColor = color(light = "#111111", dark = "#EEEEEE"),
        )
        assertEquals("#111111", text.textColor?.light)
        assertEquals("#EEEEEE", text.textColor?.dark)

        val image = scope.image(
            id = "img",
            url = "https://example.com/a.png",
            backgroundColor = color("#222222"),
            textColor = color("#333333"),
        )
        assertEquals("#222222", image.backgroundColor?.light)
        assertEquals("#333333", image.textColor?.light)

        val list = scope.list(
            id = "list",
            backgroundColor = color("#444444"),
        ) {
            text(id = "in-list", text = "Item")
        }
        assertEquals("#444444", list.backgroundColor?.light)

        val card = scope.card(
            id = "card",
            title = "Title",
            subtitle = "Subtitle",
            badge = "New",
            action = openDetails,
            titleColor = color("#010101"),
            subtitleColor = color("#020202"),
            badgeTextColor = color("#030303"),
            badgeBackgroundColor = color("#040404"),
            backgroundColor = color("#050505"),
        )
        assertEquals("#010101", card.titleColor?.light)
        assertEquals("#050505", card.backgroundColor?.light)

        val grid = scope.cardGrid(
            id = "grid",
            backgroundColor = color("#060606"),
        ) {
            card(
                id = "grid-card",
                title = "Grid",
                action = openDetails,
                backgroundColor = color("#070707"),
            )
        }
        assertEquals("#060606", grid.backgroundColor?.light)
        assertEquals("#070707", grid.items.first().backgroundColor?.light)

        val tabs = scope.tabs(
            id = "tabs",
            selectedTabTextColor = color("#111111"),
            unselectedTabTextColor = color("#222222"),
            selectedTabBackgroundColor = color("#333333"),
            unselectedTabBackgroundColor = color("#444444"),
        ) {
            tab(
                id = "tab-a",
                title = "A",
                action = selectTab,
                badge = "1",
                badgeTextColor = color("#AAAAAA"),
                badgeBackgroundColor = color("#BBBBBB"),
            )
        }
        assertEquals("#111111", tabs.selectedTabTextColor?.light)
        assertEquals("#AAAAAA", tabs.tabs.first().badgeTextColor?.light)

        val textField = scope.textField(
            id = "field",
            label = "Name",
            placeholder = "Enter",
            action = goCatalog,
            textColor = color("#101010"),
            labelColor = color("#202020"),
            placeholderColor = color("#303030"),
            backgroundColor = color("#404040"),
        )
        assertEquals("#404040", textField.backgroundColor?.light)

        val dropdown = scope.dropdown(
            id = "drop",
            label = "Pick",
            options = listOf("A", "B"),
            action = goCatalog,
            labelColor = color("#505050"),
            selectedTextColor = color("#606060"),
            backgroundColor = color("#707070"),
        )
        assertEquals("#606060", dropdown.selectedTextColor?.light)

        val slider = scope.slider(
            id = "slider",
            value = 0.5f,
            rangeStart = 0f,
            rangeEnd = 1f,
            action = goCatalog,
            textColor = color("#111111"),
            thumbColor = color("#222222"),
            activeTrackColor = color("#333333"),
            inactiveTrackColor = color("#444444"),
        )
        assertEquals("#222222", slider.thumbColor?.light)

        val switch = scope.switch(
            id = "switch",
            title = "Enabled",
            checked = true,
            action = goCatalog,
            titleColor = color("#111111"),
            checkedThumbColor = color("#222222"),
            uncheckedThumbColor = color("#333333"),
            checkedTrackColor = color("#444444"),
            uncheckedTrackColor = color("#555555"),
        )
        assertEquals("#444444", switch.checkedTrackColor?.light)

        val chips = scope.chipGroup(
            id = "chips",
            chipTextColor = color("#111111"),
            chipBackgroundColor = color("#222222"),
            selectedChipTextColor = color("#333333"),
            selectedChipBackgroundColor = color("#444444"),
        ) {
            chip(
                id = "chip-1",
                label = "Chip",
                selected = true,
                action = chipAction,
                textColor = color("#AAAAAA"),
                backgroundColor = color("#BBBBBB"),
                selectedTextColor = color("#CCCCCC"),
                selectedBackgroundColor = color("#DDDDDD"),
            )
        }
        assertEquals("#111111", chips.chipTextColor?.light)
        assertEquals("#DDDDDD", chips.chips.first().selectedBackgroundColor?.light)

        val carousel = scope.carousel(
            id = "carousel",
            backgroundColor = color("#999999"),
        ) {
            image(id = "car-img", url = "https://example.com/b.png", backgroundColor = color("#111111"))
        }
        assertEquals("#999999", carousel.backgroundColor?.light)

        val modalContent = container(id = "modal-content", ctx = ctx) { text(id = "mt", text = "Modal") }
        val modal = scope.modal(
            id = "modal",
            content = modalContent,
            primaryAction = modalPrimary,
            dismissAction = modalDismiss,
            backgroundColor = color(light = "#FFFFFF", dark = "#000000"),
            scrimColor = color("#88000000"),
        )
        assertEquals("#FFFFFF", modal.backgroundColor?.light)
        assertEquals("#88000000", modal.scrimColor?.light)

        val snackbar = scope.snackbar(
            id = "snack",
            message = "Hi",
            actionText = "OK",
            action = goCatalog,
            messageColor = color("#111111"),
            backgroundColor = color("#222222"),
            actionTextColor = color("#333333"),
        )
        assertEquals("#333333", snackbar.actionTextColor?.light)

        val state = scope.state(
            id = "state",
            state = StateKind.Error,
            message = "Oops",
            action = goCatalog,
            textColor = color("#111111"),
            backgroundColor = color("#222222"),
            actionTextColor = color("#333333"),
        )
        assertEquals("#222222", state.backgroundColor?.light)

        val progress = scope.progress(
            id = "progress",
            style = ProgressStyle.Linear,
            progress = 0.3f,
            indicatorColor = color("#111111"),
            trackColor = color("#222222"),
        )
        assertEquals("#111111", progress.indicatorColor?.light)

        val map = scope.map(
            id = "map",
            title = "Map",
            subtitle = "Subtitle",
            titleColor = color("#111111"),
            subtitleColor = color("#222222"),
            backgroundColor = color("#333333"),
        )
        assertEquals("#333333", map.backgroundColor?.light)

        val bottomBar = scope.bottomBar(
            selectedTabId = "home",
            containerColor = color("#010203"),
            selectedIconColor = color("#111111"),
            unselectedIconColor = color("#222222"),
            selectedLabelColor = color("#333333"),
            unselectedLabelColor = color("#444444"),
        ) {
            tab(
                id = "home",
                title = "Home",
                action = goCatalog,
                badge = "9",
                badgeTextColor = color("#AAAAAA"),
                badgeBackgroundColor = color("#BBBBBB"),
            )
        }
        assertNotNull(bottomBar.containerColor)
        assertEquals("#010203", bottomBar.containerColor?.light)
        assertEquals("#AAAAAA", bottomBar.tabs.first().badgeTextColor?.light)

        val registeredIds = ctx.actions.map { it.id }
        assertTrue(registeredIds.contains(goCatalog.id))
        assertTrue(registeredIds.contains(openDetails.id))
        assertTrue(registeredIds.contains(selectTab.id))
        assertTrue(registeredIds.contains(chipAction.id))
        assertTrue(registeredIds.contains(modalPrimary.id))
        assertTrue(registeredIds.contains(modalDismiss.id))
    }
}


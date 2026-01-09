package org.igorv8836.bdui.contract

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ColorCoverageSerializationTest {

    private val json = Json {
        encodeDefaults = false
        classDiscriminator = "type"
    }

    @Test
    fun `component tree keeps color fields`() {
        val root = Container(
            id = "root",
            direction = ContainerDirection.Column,
            backgroundColor = Color(light = "#000000", dark = "#FFFFFF"),
            children = listOf(
                TextElement(
                    id = "text",
                    text = "Hello",
                    textColor = Color("#111111"),
                ),
                ButtonElement(
                    id = "cta",
                    title = "Open",
                    actionId = "go",
                    textColor = Color("#222222"),
                    backgroundColor = Color("#333333"),
                ),
                CardElement(
                    id = "card",
                    title = "Title",
                    subtitle = "Subtitle",
                    badge = "New",
                    titleColor = Color("#444444"),
                    subtitleColor = Color("#555555"),
                    badgeTextColor = Color("#666666"),
                    badgeBackgroundColor = Color("#777777"),
                    backgroundColor = Color("#888888"),
                ),
                TabsElement(
                    id = "tabs",
                    tabs = listOf(
                        TabItem(
                            id = "tab-1",
                            title = "Tab",
                            actionId = "select",
                            badge = "1",
                            badgeTextColor = Color("#AAAAAA"),
                            badgeBackgroundColor = Color("#BBBBBB"),
                        ),
                    ),
                    selectedTabTextColor = Color("#123123"),
                    unselectedTabTextColor = Color("#321321"),
                ),
                ModalElement(
                    id = "modal",
                    content = TextElement(id = "modal-text", text = "Modal"),
                    backgroundColor = Color("#010203"),
                    scrimColor = Color("#0A0B0C"),
                ),
                SnackbarElement(
                    id = "snack",
                    message = "Hi",
                    actionText = "OK",
                    actionId = "snack-action",
                    messageColor = Color("#111111"),
                    backgroundColor = Color("#222222"),
                    actionTextColor = Color("#333333"),
                ),
                ProgressElement(
                    id = "progress",
                    indicatorColor = Color("#111111"),
                    trackColor = Color("#222222"),
                ),
                MapElement(
                    id = "map",
                    title = "Map",
                    subtitle = "Subtitle",
                    titleColor = Color("#111111"),
                    subtitleColor = Color("#222222"),
                    backgroundColor = Color("#333333"),
                ),
            ),
        )

        val encoded = json.encodeToString(root)
        val decoded = json.decodeFromString<Container>(encoded)

        assertEquals("#000000", decoded.backgroundColor?.light)
        assertEquals("#FFFFFF", decoded.backgroundColor?.dark)

        val decodedText = assertIs<TextElement>(decoded.children[0])
        assertEquals("#111111", decodedText.textColor?.light)

        val decodedButton = assertIs<ButtonElement>(decoded.children[1])
        assertEquals("#222222", decodedButton.textColor?.light)
        assertEquals("#333333", decodedButton.backgroundColor?.light)

        val decodedCard = assertIs<CardElement>(decoded.children[2])
        assertEquals("#444444", decodedCard.titleColor?.light)
        assertEquals("#888888", decodedCard.backgroundColor?.light)

        val decodedTabs = assertIs<TabsElement>(decoded.children[3])
        assertEquals("#123123", decodedTabs.selectedTabTextColor?.light)
        assertEquals("#AAAAAA", decodedTabs.tabs.first().badgeTextColor?.light)

        val decodedModal = assertIs<ModalElement>(decoded.children[4])
        assertEquals("#010203", decodedModal.backgroundColor?.light)
        assertEquals("#0A0B0C", decodedModal.scrimColor?.light)

        val decodedSnack = assertIs<SnackbarElement>(decoded.children[5])
        assertEquals("#111111", decodedSnack.messageColor?.light)

        val decodedProgress = assertIs<ProgressElement>(decoded.children[6])
        assertEquals("#222222", decodedProgress.trackColor?.light)

        val decodedMap = assertIs<MapElement>(decoded.children[7])
        assertEquals("#333333", decodedMap.backgroundColor?.light)
    }

    @Test
    fun `bottom bar keeps color fields`() {
        val scaffold = Scaffold(
            bottomBar = BottomBar(
                tabs = listOf(
                    BottomTab(
                        id = "home",
                        title = "Home",
                        actionId = "go-home",
                        badge = "9",
                        badgeTextColor = Color("#111111"),
                        badgeBackgroundColor = Color("#222222"),
                    ),
                ),
                selectedTabId = "home",
                containerColor = Color("#010203"),
                selectedIconColor = Color("#111111"),
                unselectedIconColor = Color("#222222"),
                selectedLabelColor = Color("#333333"),
                unselectedLabelColor = Color("#444444"),
            ),
        )

        val encoded = json.encodeToString(scaffold)
        val decoded = json.decodeFromString<Scaffold>(encoded)

        val bar = decoded.bottomBar!!
        assertEquals("#010203", bar.containerColor?.light)
        assertEquals("#111111", bar.tabs.first().badgeTextColor?.light)
        assertEquals("#222222", bar.tabs.first().badgeBackgroundColor?.light)
    }
}


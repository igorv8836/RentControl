package org.igorv8836.bdui.demo

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.igorv8836.bdui.contract.Action
import org.igorv8836.bdui.contract.CardElement
import org.igorv8836.bdui.contract.CardGridElement
import org.igorv8836.bdui.contract.ChipGroupElement
import org.igorv8836.bdui.contract.ChipItem
import org.igorv8836.bdui.contract.Container
import org.igorv8836.bdui.contract.ContainerDirection
import org.igorv8836.bdui.contract.Layout
import org.igorv8836.bdui.contract.RemoteScreen
import org.igorv8836.bdui.contract.ScreenSettings
import org.igorv8836.bdui.contract.SliderElement
import org.igorv8836.bdui.contract.SwitchElement
import org.igorv8836.bdui.contract.TabItem
import org.igorv8836.bdui.contract.TabsElement
import org.igorv8836.bdui.contract.TextElement
import org.igorv8836.bdui.contract.TextFieldElement
import org.igorv8836.bdui.contract.VariableScope
import org.igorv8836.bdui.contract.VariableValue
import org.igorv8836.bdui.core.actions.ActionHandler
import org.igorv8836.bdui.core.actions.ActionRegistry
import org.igorv8836.bdui.renderer.host.ScreenHost
import org.igorv8836.bdui.runtime.ScreenState
import org.igorv8836.bdui.runtime.ScreenStatus
import org.igorv8836.bdui.runtime.VariableStoreImpl
import org.igorv8836.bdui.testing.TestNavigator
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ComplexComponentsUiTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun interactsWithAdvancedComponents() {
        val actionsFired = mutableListOf<String>()
        val actionHandler = ActionHandler<TestAction> { action, _ ->
            actionsFired += action.id
        }
        val registry = ActionRegistry(
            handlersByType = mapOf(
                TestAction::class to actionHandler,
            ),
        )
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
        val variables = VariableStoreImpl(scope = scope)

        val screen = RemoteScreen(
            id = "complex",
            version = 1,
            layout = Layout(
                root = Container(
                    id = "root",
                    direction = ContainerDirection.Column,
                    children = listOf(
                        CardGridElement(
                            id = "cards",
                            columns = 2,
                            items = listOf(
                                CardElement(id = "c1", title = "Card 1", actionId = "card-1"),
                                CardElement(id = "c2", title = "Card 2", subtitle = "Sub", actionId = "card-2"),
                            ),
                        ),
                        TabsElement(
                            id = "tabs",
                            tabs = listOf(
                                TabItem(id = "t1", title = "Tab A", actionId = "tab-a"),
                                TabItem(id = "t2", title = "Tab B", actionId = "tab-b", badge = "2"),
                            ),
                            selectedTabId = "t1",
                        ),
                        TextFieldElement(
                            id = "tf",
                            label = "Name",
                            value = "John",
                            actionId = "text-change",
                        ),
                        SwitchElement(
                            id = "sw",
                            title = "Enable",
                            checked = false,
                            actionId = "switch",
                        ),
                        ChipGroupElement(
                            id = "chips",
                            chips = listOf(
                                ChipItem(id = "ch1", label = "Chip1", actionId = "chip-1"),
                                ChipItem(id = "ch2", label = "Chip2", actionId = "chip-2"),
                            ),
                            singleSelection = true,
                        ),
                        SliderElement(
                            id = "slider",
                            value = 0.5f,
                            rangeStart = 0f,
                            rangeEnd = 1f,
                            actionId = "slider",
                        ),
                        TextElement(id = "footer", text = "Done"),
                    ),
                ),
            ),
            settings = ScreenSettings(scrollable = true),
            actions = listOf(
                TestAction("card-1"),
                TestAction("card-2"),
                TestAction("tab-a"),
                TestAction("tab-b"),
                TestAction("text-change"),
                TestAction("switch"),
                TestAction("chip-1"),
                TestAction("chip-2"),
                TestAction("slider"),
            ),
        )

        composeRule.setContent {
            ScreenHost(
                state = ScreenState(remoteScreen = screen, status = ScreenStatus.Ready, empty = false),
                actionRegistry = registry,
                variableStore = variables,
                navigator = TestNavigator,
            )
        }

        // Cards
        composeRule.onNodeWithText("Card 1").assertIsDisplayed().performClick()
        composeRule.onNodeWithText("Card 2").assertIsDisplayed().performClick()

        // Tabs
        composeRule.onNodeWithText("Tab B").performClick()

        // Switch
        composeRule.onNodeWithText("Enable").performClick()

        // Chips
        composeRule.onNodeWithText("Chip2").performClick()

        assertEquals(
            setOf("card-1", "card-2", "tab-b", "switch", "chip-2"),
            actionsFired.toSet()
        )
    }
}
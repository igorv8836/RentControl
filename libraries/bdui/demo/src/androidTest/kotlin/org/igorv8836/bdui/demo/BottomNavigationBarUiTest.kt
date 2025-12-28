package org.igorv8836.bdui.demo

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.igorv8836.bdui.actions.variables.SetVariableAction
import org.igorv8836.bdui.contract.Action
import org.igorv8836.bdui.contract.BottomBar
import org.igorv8836.bdui.contract.BottomTab
import org.igorv8836.bdui.contract.Container
import org.igorv8836.bdui.contract.ContainerDirection
import org.igorv8836.bdui.contract.Layout
import org.igorv8836.bdui.contract.RemoteScreen
import org.igorv8836.bdui.contract.ScreenSettings
import org.igorv8836.bdui.contract.TextElement
import org.igorv8836.bdui.contract.TextStyle
import org.igorv8836.bdui.contract.VariableScope
import org.igorv8836.bdui.contract.VariableValue
import org.igorv8836.bdui.core.actions.ActionHandler
import org.igorv8836.bdui.core.actions.ActionRegistry
import org.igorv8836.bdui.core.navigation.Navigator
import org.igorv8836.bdui.engine.ScreenEngineFactory
import org.igorv8836.bdui.renderer.host.ScreenHost
import org.igorv8836.bdui.runtime.ScreenRepository
import org.igorv8836.bdui.runtime.ScreenState
import org.igorv8836.bdui.runtime.ScreenStatus
import org.igorv8836.bdui.runtime.VariableStoreImpl
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import org.igorv8836.bdui.testing.TestNavigator

class BottomNavigationBarUiTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun rendersCustomLabelAndIcon_andDispatchesActionOnClick() {
        val clickAction = SetVariableAction(
            id = "open-catalog",
            key = "clicked",
            value = VariableValue.StringValue("catalog"),
        )
        val screen = RemoteScreen(
            id = "home",
            version = 1,
            layout = Layout(
                root = Container(
                    id = "root",
                    direction = ContainerDirection.Column,
                    children = listOf(
                        TextElement(id = "body", text = "Body", style = TextStyle.Body)
                    ),
                ),
                scaffold = org.igorv8836.bdui.contract.Scaffold(
                    bottomBar = BottomBar(
                        selectedTabId = "tab-1",
                        tabs = listOf(
                            BottomTab(
                                id = "tab-1",
                                title = "Home",
                                actionId = "open-home",
                                label = TextElement(id = "lbl1", text = "LBL1", style = TextStyle.Body),
                                icon = TextElement(id = "icon1", text = "ICON1", style = TextStyle.Body),
                            ),
                            BottomTab(
                                id = "tab-2",
                                title = "Catalog",
                                actionId = clickAction.id,
                                label = TextElement(id = "lbl2", text = "LBL2", style = TextStyle.Body),
                                icon = TextElement(id = "icon2", text = "ICON2", style = TextStyle.Body),
                            ),
                        ),
                    ),
                ),
            ),
            settings = ScreenSettings(scrollable = false),
            actions = listOf(clickAction),
        )

        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
        val engine = ScreenEngineFactory(
            repository = object : ScreenRepository {
                override suspend fun fetch(
                    screenId: String,
                    params: Map<String, String>,
                ): Result<RemoteScreen> = Result.success(screen)
            },
            navigator = TestNavigator,
            variableStore = VariableStoreImpl(scope = scope),
        ).create(screen.id, scope)

        engine.show(screen)

        composeRule.setContent {
            ScreenHost(
                state = ScreenState(remoteScreen = screen, status = ScreenStatus.Ready, empty = false),
                actionRegistry = engine.actionRegistry,
                variableStore = engine.variableStore,
                navigator = TestNavigator,
            )
        }

        // Custom label/icon from backend are rendered
        composeRule.onNodeWithText("LBL1").assertIsDisplayed()

        // Action dispatched on tab click
        composeRule.onNodeWithText("LBL2").performClick()
        val stored = engine.variableStore.peek("clicked", VariableScope.Global, null) as VariableValue.StringValue
        assertEquals("catalog", stored.value)
    }

    @Test
    fun switchesBottomBarWhenScreenChanges() {
        val homeScreen = RemoteScreen(
            id = "home",
            version = 1,
            layout = Layout(
                root = TextElement(id = "body", text = "Home", style = TextStyle.Body),
                scaffold = org.igorv8836.bdui.contract.Scaffold(
                    bottomBar = BottomBar(
                        selectedTabId = "tab-home",
                        tabs = listOf(
                            BottomTab(
                                id = "tab-home",
                                title = "Home",
                                actionId = "open-home",
                                label = TextElement(id = "home-label", text = "HOME-LBL", style = TextStyle.Body),
                            ),
                            BottomTab(
                                id = "tab-catalog",
                                title = "Catalog",
                                actionId = "open-catalog",
                                label = TextElement(id = "cat-label", text = "CAT-LBL", style = TextStyle.Body),
                            ),
                        ),
                    ),
                ),
            ),
            settings = ScreenSettings(scrollable = false),
            actions = listOf(TestAction("open-catalog")),
        )
        val catalogScreen = RemoteScreen(
            id = "catalog",
            version = 1,
            layout = Layout(
                root = TextElement(id = "body2", text = "Catalog", style = TextStyle.Body),
                scaffold = org.igorv8836.bdui.contract.Scaffold(
                    bottomBar = BottomBar(
                        selectedTabId = "tab-catalog",
                        tabs = listOf(
                            BottomTab(
                                id = "tab-catalog",
                                title = "Catalog",
                                actionId = "open-catalog",
                                label = TextElement(id = "cat2-label", text = "CAT2-LBL", style = TextStyle.Body),
                            ),
                        ),
                    ),
                ),
            ),
            settings = ScreenSettings(scrollable = false),
        )

        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
        val variableStore = VariableStoreImpl(scope = scope)
        var uiState by mutableStateOf(
            ScreenState(remoteScreen = homeScreen, status = ScreenStatus.Ready, empty = false)
        )

        val registry = ActionRegistry(
            handlersByType = mapOf(
                TestAction::class to ActionHandler<TestAction> { _, _ ->
                    uiState = ScreenState(remoteScreen = catalogScreen, status = ScreenStatus.Ready, empty = false)
                },
            ),
        )

        composeRule.setContent {
            ScreenHost(
                state = uiState,
                actionRegistry = registry,
                variableStore = variableStore,
                navigator = TestNavigator,
            )
        }

        // Initially shows home bottom bar label
        composeRule.onNodeWithText("HOME-LBL").assertIsDisplayed()

        // Click catalog tab to switch screen/bottom bar
        composeRule.onNodeWithText("CAT-LBL").performClick()

        // New bottom bar from catalog screen should be visible, old hidden
        composeRule.onNodeWithText("CAT2-LBL").assertIsDisplayed()
        composeRule.onNodeWithText("HOME-LBL").assertDoesNotExist()
    }
}

data class TestAction(override val id: String) : Action

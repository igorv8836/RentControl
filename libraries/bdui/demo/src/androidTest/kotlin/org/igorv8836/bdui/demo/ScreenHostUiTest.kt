package org.igorv8836.bdui.demo

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.igorv8836.bdui.contract.Layout
import org.igorv8836.bdui.contract.RemoteScreen
import org.igorv8836.bdui.contract.ScreenSettings
import org.igorv8836.bdui.contract.TextElement
import org.igorv8836.bdui.contract.TextStyle
import org.igorv8836.bdui.contract.ButtonElement
import org.igorv8836.bdui.contract.Container
import org.igorv8836.bdui.contract.ContainerDirection
import org.igorv8836.bdui.contract.VariableValue
import org.igorv8836.bdui.actions.variables.SetVariableAction
import org.igorv8836.bdui.contract.VariableScope
import org.igorv8836.bdui.core.navigation.Navigator
import org.igorv8836.bdui.engine.ScreenEngineFactory
import org.igorv8836.bdui.renderer.ScreenHost
import org.igorv8836.bdui.runtime.ScreenRepository
import org.igorv8836.bdui.runtime.ScreenState
import org.igorv8836.bdui.runtime.ScreenStatus
import org.igorv8836.bdui.runtime.VariableStoreImpl
import org.junit.Assert.assertEquals
import org.junit.Test

class ScreenHostUiTest {

    @get:org.junit.Rule
    val composeRule = createComposeRule()

    @Test
    fun rendersBasicText() {
        val screen = RemoteScreen(
            id = "home",
            version = 1,
            layout = Layout(root = TextElement(id = "welcome", text = "Hello", style = TextStyle.Body)),
            settings = ScreenSettings(scrollable = false),
        )
        val engine = ScreenEngineFactory(
            repository = object : ScreenRepository {
                override suspend fun fetch(screenId: String, params: Map<String, String>): Result<RemoteScreen> =
                    Result.success(screen)
            },
            navigator = NoopNavigator,
            variableStore = VariableStoreImpl(scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)),
        ).create("home", CoroutineScope(SupervisorJob() + Dispatchers.Main))

        // Preload state to mimic successful fetch
        engine.show(screen)

        composeRule.setContent {
            ScreenHost(
                state = ScreenState(remoteScreen = screen, status = ScreenStatus.Ready, empty = false),
                actionRegistry = engine.actionRegistry,
                variableStore = engine.variableStore,
                navigator = NoopNavigator,
            )
        }

        composeRule.onNodeWithText("Hello").assertIsDisplayed()
        assertEquals(ScreenStatus.Ready, engine.state.value.status)
    }

    @Test
    fun updatesTextWhenVariableChangesViaAction() {
        val setName = SetVariableAction(
            id = "set-name",
            key = "name",
            value = VariableValue.StringValue("Tester"),
        )
        val screen = RemoteScreen(
            id = "home",
            version = 1,
            layout = Layout(
                root = Container(
                    id = "root",
                    direction = ContainerDirection.Column,
                    children = listOf(
                        TextElement(
                            id = "welcome",
                            text = "welcome",
                            template = "Hi @{name}",
                            style = TextStyle.Body,
                        ),
                        ButtonElement(
                            id = "btn-set",
            title = "Set name",
                            actionId = setName.id,
                        ),
                    ),
                ),
            ),
            settings = ScreenSettings(scrollable = false),
            actions = listOf(setName),
        )
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
        val engine = ScreenEngineFactory(
            repository = object : ScreenRepository {
                override suspend fun fetch(screenId: String, params: Map<String, String>): Result<RemoteScreen> =
                    Result.success(screen)
            },
            navigator = NoopNavigator,
            variableStore = VariableStoreImpl(scope = scope),
        ).create("home", scope)

        engine.show(screen)

        composeRule.setContent {
            ScreenHost(
                state = ScreenState(remoteScreen = screen, status = ScreenStatus.Ready, empty = false),
                actionRegistry = engine.actionRegistry,
                variableStore = engine.variableStore,
                navigator = NoopNavigator,
            )
        }

        // Trigger action to set the variable
        composeRule.onNodeWithText("Set name").performClick()
        composeRule.onNodeWithText("Hi Tester").assertIsDisplayed()

        val stored = engine.variableStore.peek("name", VariableScope.Global, null) as VariableValue.StringValue
        assertEquals("Tester", stored.value)
    }
}

private object NoopNavigator : Navigator {
    override fun openRoute(route: org.igorv8836.bdui.contract.Route, parameters: Map<String, String>) {}
    override fun forward(path: String?, remoteScreen: org.igorv8836.bdui.contract.RemoteScreen?, parameters: Map<String, String>) {}
    override fun showPopup(popup: org.igorv8836.bdui.contract.Popup, parameters: Map<String, String>) {}
    override fun showOverlay(overlay: org.igorv8836.bdui.contract.Overlay, parameters: Map<String, String>) {}
}

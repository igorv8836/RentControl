package org.igorv8836.bdui.testing

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.igorv8836.bdui.actions.navigation.ForwardAction
import org.igorv8836.bdui.contract.Action
import org.igorv8836.bdui.contract.ButtonElement
import org.igorv8836.bdui.contract.ButtonKind
import org.igorv8836.bdui.contract.Condition
import org.igorv8836.bdui.contract.Container
import org.igorv8836.bdui.contract.ContainerDirection
import org.igorv8836.bdui.contract.Layout
import org.igorv8836.bdui.contract.Overlay
import org.igorv8836.bdui.contract.Popup
import org.igorv8836.bdui.contract.RemoteScreen
import org.igorv8836.bdui.contract.Route
import org.igorv8836.bdui.contract.Semantics
import org.igorv8836.bdui.contract.StoragePolicy
import org.igorv8836.bdui.contract.TextElement
import org.igorv8836.bdui.contract.VariableScope
import org.igorv8836.bdui.contract.VariableValue
import org.igorv8836.bdui.core.actions.ActionRegistry
import org.igorv8836.bdui.core.context.ActionContext
import org.igorv8836.bdui.core.context.ScreenContext
import org.igorv8836.bdui.core.navigation.Navigator
import org.igorv8836.bdui.core.variables.VariableStore
import org.igorv8836.bdui.runtime.ScreenRepository

object TestNavigator : Navigator {
    override fun openRoute(route: Route, parameters: Map<String, String>) {}
    override fun forward(path: String?, remoteScreen: RemoteScreen?, parameters: Map<String, String>) {}
    override fun showPopup(popup: Popup, parameters: Map<String, String>) {}
    override fun showOverlay(overlay: Overlay, parameters: Map<String, String>) {}
}

object TestVariableStore : VariableStore {
    private val state = MutableStateFlow(0L)
    override val changes: StateFlow<Long> = state
    override suspend fun get(key: String, scope: VariableScope, screenId: String?): VariableValue? = null
    override fun peek(key: String, scope: VariableScope, screenId: String?): VariableValue? = null
    override suspend fun set(key: String, value: VariableValue, scope: VariableScope, screenId: String?, policy: StoragePolicy, ttlMillis: Long?) {}
    override suspend fun increment(key: String, delta: Double, scope: VariableScope, screenId: String?, policy: StoragePolicy) {}
    override suspend fun remove(key: String, scope: VariableScope, screenId: String?) {}
    override suspend fun syncFromPersistent(screenId: String?) {}
    override fun dispose() {}
}

fun testActionContext(
    actionRegistry: ActionRegistry = ActionRegistry(emptyMap()),
    screenId: String = "test",
): ActionContext = ActionContext(
    navigator = TestNavigator,
    screenContext = ScreenContext(
        variableStore = TestVariableStore,
        actionRegistry = actionRegistry,
    ),
    screenId = screenId,
)

object Fixtures {

    fun screenWithTextAndButton(): RemoteScreen {
        val action = ForwardAction(id = "continue", path = "/demo/next")
        return RemoteScreen(
            id = "demo",
            version = 1,
            layout = Layout(
                root = Container(
                    id = "root",
                    direction = ContainerDirection.Column,
                    children = listOf(
                        TextElement(id = "title", text = "title"),
                        ButtonElement(
                            id = "cta",
                            title = "cta",
                            actionId = action.id,
                            kind = ButtonKind.Primary,
                        ),
                    ),
                ),
            ),
            actions = listOf(action),
        )
    }
}

class FakeScreenRepository(
    private val remoteScreen: RemoteScreen = Fixtures.screenWithTextAndButton(),
    private val delayMs: Long = 0,
) : ScreenRepository {

    override suspend fun fetch(screenId: String, params: Map<String, String>): Result<RemoteScreen> {
        if (delayMs > 0) {
            delay(delayMs)
        }
        return Result.success(remoteScreen.copy(id = screenId))
    }
}

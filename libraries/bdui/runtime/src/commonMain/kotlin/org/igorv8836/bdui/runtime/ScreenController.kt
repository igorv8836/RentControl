package org.igorv8836.bdui.runtime

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.igorv8836.bdui.actions.ActionHandler
import org.igorv8836.bdui.actions.ActionContext
import org.igorv8836.bdui.actions.ActionExecutor
import org.igorv8836.bdui.actions.ActionRegistry
import org.igorv8836.bdui.actions.Navigator
import org.igorv8836.bdui.actions.Router
import org.igorv8836.bdui.contract.Screen
import org.igorv8836.bdui.contract.UiEvent

interface ScreenController {
    val state: StateFlow<ScreenState>
    fun onOpen()
    fun onAppear()
    fun onFullyVisible()
    fun onDisappear()
    fun dispose()
}

class DefaultScreenController(
    private val screenId: String,
    private val repository: ScreenRepository,
    private val navigator: Navigator,
    private val router: Router,
    private val analytics: (String, Map<String, String>) -> Unit = { _, _ -> },
    private val externalScope: CoroutineScope,
) : ScreenController {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(externalScope.coroutineContext + job)
    private val store = ScreenStore(repository, scope)
    private val executor = ActionExecutor(navigator = navigator, analytics = analytics)
    private val actionRegistry = ActionRegistry(
        handlers = emptyMap<String, ActionHandler>(),
        fallback = null,
        executor = executor,
    )
    private val pendingLifecycle = MutableStateFlow(emptyList<UiEvent>())

    override val state: StateFlow<ScreenState> = store.state

    init {
        scope.launch {
            state.collectLatest { snapshot ->
                val screen = snapshot.screen
                if (screen != null && pendingLifecycle.value.isNotEmpty()) {
                    val events = pendingLifecycle.value
                    pendingLifecycle.value = emptyList()
                    dispatchLifecycle(events, screen)
                }
            }
        }
    }

    override fun onOpen() {
        store.load(screenId)
        runLifecycle(state.value.screen?.lifecycle?.onOpen.orEmpty())
    }

    override fun onAppear() {
        runLifecycle(state.value.screen?.lifecycle?.onAppear.orEmpty())
    }

    override fun onFullyVisible() {
        runLifecycle(state.value.screen?.lifecycle?.onFullyVisible.orEmpty())
    }

    override fun onDisappear() {
        runLifecycle(state.value.screen?.lifecycle?.onDisappear.orEmpty())
    }

    override fun dispose() {
        job.cancel()
    }

    fun refresh(params: Map<String, String> = emptyMap()) {
        store.refresh(params = params)
    }

    fun loadNextPage() {
        // Placeholder for pagination effect; repo can be extended to support pagination params.
        store.setLoadingMore(true)
        store.setLoadingMore(false)
    }

    fun dispatch(actionId: String) {
        val screen = state.value.screen ?: return
        val action = screen.actions.firstOrNull { it.id == actionId } ?: return
        scope.launch {
            actionRegistry.dispatch(
                action,
                ActionContext(
                    router = router,
                    analytics = analytics,
                    navigator = navigator,
                ),
            )
        }
    }

    private fun runLifecycle(events: List<UiEvent>) {
        val screen = state.value.screen
        if (screen == null) {
            if (events.isNotEmpty()) {
                pendingLifecycle.value = pendingLifecycle.value + events
            }
            return
        }
        dispatchLifecycle(events, screen)
    }

    private fun dispatchLifecycle(events: List<UiEvent>, screen: Screen) {
        events.flatMap { it.actions }.forEach { action ->
            scope.launch {
                actionRegistry.dispatch(
                    action,
                    ActionContext(
                        router = router,
                        analytics = analytics,
                        navigator = navigator,
                    ),
                )
            }
        }
    }
}

class ScreenControllerFactory(
    private val repository: ScreenRepository,
    private val navigator: Navigator,
    private val router: Router,
    private val analytics: (String, Map<String, String>) -> Unit = { _, _ -> },
) {
    fun create(screenId: String, scope: CoroutineScope): DefaultScreenController =
        DefaultScreenController(
            screenId = screenId,
            repository = repository,
            navigator = navigator,
            router = router,
            analytics = analytics,
            externalScope = scope,
        )
}

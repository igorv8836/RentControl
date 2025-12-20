package org.igorv8836.bdui.runtime

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.igorv8836.bdui.actions.ActionHandler
import org.igorv8836.bdui.actions.ActionContext
import org.igorv8836.bdui.actions.ActionExecutor
import org.igorv8836.bdui.actions.ActionRegistry
import org.igorv8836.bdui.actions.Navigator
import org.igorv8836.bdui.actions.Router
import org.igorv8836.bdui.actions.VariableAdapter
import org.igorv8836.bdui.contract.Screen
import org.igorv8836.bdui.contract.ScreenEventType
import org.igorv8836.bdui.contract.StoragePolicy
import org.igorv8836.bdui.contract.UiEvent
import org.igorv8836.bdui.contract.VariableScope
import org.igorv8836.bdui.contract.VariableValue

interface ScreenController {
    val state: StateFlow<ScreenState>
    val variableStore: VariableStore
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
    providedVariableStore: VariableStore? = null,
) : ScreenController {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(externalScope.coroutineContext + job)
    private val store = ScreenStore(repository, scope)
    override val variableStore: VariableStore = providedVariableStore ?: VariableStore(scope = externalScope)
    private val variableAdapter: VariableAdapter = VariableStoreAdapter(variableStore)
    private val executor = ActionExecutor(
        navigator = navigator,
        analytics = analytics,
        variables = variableAdapter,
    )
    private val actionRegistry = ActionRegistry(
        handlers = emptyMap<String, ActionHandler>(),
        fallback = null,
        executor = executor,
    )
    private val pendingLifecycle = MutableStateFlow(emptyList<UiEvent>())
    private var triggerEngine: TriggerEngine? = null
    private var activeScreenId: String? = null
    private var previousState: ScreenState? = null

    override val state: StateFlow<ScreenState> = store.state

    init {
        scope.launch {
            state.collect { snapshot ->
                val screen = snapshot.screen
                if (screen != null) {
                    if (triggerEngine == null || activeScreenId != screen.id) {
                        triggerEngine?.stop()
                        triggerEngine = TriggerEngine(
                            screenId = screen.id,
                            variableStore = variableStore,
                            variableAdapter = variableAdapter,
                            actionRegistry = actionRegistry,
                            router = router,
                            analytics = analytics,
                            navigator = navigator,
                            externalScope = scope,
                        ).also { engine ->
                            engine.start(screen.triggers)
                        }
                        activeScreenId = screen.id
                    }
                    if (pendingLifecycle.value.isNotEmpty()) {
                        val events = pendingLifecycle.value
                        pendingLifecycle.value = emptyList()
                        dispatchLifecycle(events, screen)
                    }
                }
                trackScreenEvents(previousState, snapshot)
                previousState = snapshot
            }
        }
    }

    override fun onOpen() {
        store.load(screenId)
        runLifecycle(state.value.screen?.lifecycle?.onOpen.orEmpty())
        triggerEngine?.onEvent(ScreenEventType.OnOpen)
    }

    override fun onAppear() {
        runLifecycle(state.value.screen?.lifecycle?.onAppear.orEmpty())
        triggerEngine?.onEvent(ScreenEventType.OnAppear)
    }

    override fun onFullyVisible() {
        runLifecycle(state.value.screen?.lifecycle?.onFullyVisible.orEmpty())
        triggerEngine?.onEvent(ScreenEventType.OnFullyVisible)
    }

    override fun onDisappear() {
        runLifecycle(state.value.screen?.lifecycle?.onDisappear.orEmpty())
        triggerEngine?.onEvent(ScreenEventType.OnDisappear)
    }

    override fun dispose() {
        job.cancel()
        triggerEngine?.stop()
    }

    fun refresh(params: Map<String, String> = emptyMap()) {
        store.refresh(params = params)
    }

    fun loadNextPage() {
        val settings = state.value.screen?.settings?.pagination
        store.loadNextPage(settings = settings)
        triggerEngine?.onEvent(ScreenEventType.LoadNextPageCompleted)
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
                    variables = variableAdapter,
                    screenId = screenId,
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
                        variables = variableAdapter,
                        screenId = screenId,
                    ),
                )
            }
        }
    }

    private fun trackScreenEvents(previous: ScreenState?, current: ScreenState) {
        if (previous?.refreshing == true && current.refreshing == false) {
            triggerEngine?.onEvent(ScreenEventType.RefreshCompleted)
        }
    }
}

class ScreenControllerFactory(
    private val repository: ScreenRepository,
    private val navigator: Navigator,
    private val router: Router,
    private val analytics: (String, Map<String, String>) -> Unit = { _, _ -> },
    private val variableStore: VariableStore? = null,
) {
    fun create(screenId: String, scope: CoroutineScope): DefaultScreenController =
        DefaultScreenController(
            screenId = screenId,
            repository = repository,
            navigator = navigator,
            router = router,
            analytics = analytics,
            externalScope = scope,
            providedVariableStore = variableStore ?: VariableStore(scope = scope),
        )
}

private class VariableStoreAdapter(
    private val store: VariableStore,
) : VariableAdapter {
    override fun peek(key: String, scope: VariableScope, screenId: String?): VariableValue? =
        store.peek(key, scope, screenId)

    override suspend fun set(
        key: String,
        value: VariableValue,
        scope: VariableScope,
        screenId: String?,
        policy: StoragePolicy,
        ttlMillis: Long?,
    ) {
        store.set(key, value, scope, screenId, policy, ttlMillis)
    }

    override suspend fun increment(
        key: String,
        delta: Double,
        scope: VariableScope,
        screenId: String?,
        policy: StoragePolicy,
    ) {
        store.increment(key, delta, scope, screenId, policy)
    }

    override suspend fun remove(key: String, scope: VariableScope, screenId: String?) {
        store.remove(key, scope, screenId)
    }
}

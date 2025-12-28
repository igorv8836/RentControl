package org.igorv8836.bdui.engine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.igorv8836.bdui.actions.buildActionRegistry
import org.igorv8836.bdui.contract.RemoteScreen
import org.igorv8836.bdui.contract.ScreenEventType
import org.igorv8836.bdui.core.actions.ActionRegistry
import org.igorv8836.bdui.core.context.ActionContext
import org.igorv8836.bdui.core.context.ScreenContext
import org.igorv8836.bdui.core.navigation.Navigator
import org.igorv8836.bdui.core.variables.VariableStore
import org.igorv8836.bdui.logger.ConsoleLogger
import org.igorv8836.bdui.logger.LogTags
import org.igorv8836.bdui.logger.Logger
import org.igorv8836.bdui.runtime.ScreenRepository
import org.igorv8836.bdui.runtime.ScreenState
import org.igorv8836.bdui.runtime.TriggerEngine
import org.igorv8836.bdui.runtime.VariableStoreImpl
import org.igorv8836.bdui.runtime.controller.DefaultScreenController

/**
 * One engine per screen. Owns controller, variables, triggers and lifecycle wiring.
 */
class ScreenEngine internal constructor(
    private val controller: DefaultScreenController,
    val actionRegistry: ActionRegistry,
    val variableStore: VariableStore,
    val navigator: Navigator,
    private val scope: CoroutineScope,
    private val logger: Logger = ConsoleLogger(LogTags.ENGINE),
) {
    val state: StateFlow<ScreenState> = controller.state
    private val screenContext = ScreenContext(variableStore = variableStore, actionRegistry = actionRegistry)
    private var triggerEngine: TriggerEngine? = null
    private var activeScreenId: String? = null
    private var previousState: ScreenState? = null
    private val pendingEvents = mutableListOf<ScreenEventType>()

    init {
        scope.launch {
            state.collect { snapshot -> handleState(snapshot) }
        }
    }

    fun onOpen() {
        controller.onOpen()
        enqueueLifecycle(ScreenEventType.OnOpen)
    }

    fun onAppear() = enqueueLifecycle(ScreenEventType.OnAppear)
    fun onFullyVisible() = enqueueLifecycle(ScreenEventType.OnFullyVisible)
    fun onDisappear() = enqueueLifecycle(ScreenEventType.OnDisappear)
    fun refresh(params: Map<String, String> = emptyMap()) = controller.refresh(params)
    fun loadNextPage() {
        controller.loadNextPage()
        triggerEngine?.onEvent(ScreenEventType.LoadNextPageCompleted)
    }
    fun load(screenId: String, params: Map<String, String> = emptyMap()) {
        controller.load(screenId, params)
        enqueueLifecycle(ScreenEventType.OnOpen)
    }
    fun show(remoteScreen: RemoteScreen) = controller.show(remoteScreen)
    fun dispatch(actionId: String) = controller.dispatch(actionId)
    fun dispose() {
        triggerEngine?.stop()
        controller.dispose()
        variableStore.dispose()
        scope.cancel()
    }

    private fun handleState(snapshot: ScreenState) {
        val screen = snapshot.remoteScreen
        if (screen != null && activeScreenId != screen.id) {
            triggerEngine?.stop()
            triggerEngine = TriggerEngine(
                screenId = screen.id,
                variableStore = variableStore,
                actionContext = ActionContext(
                    navigator = navigator,
                    screenContext = screenContext,
                    screenId = screen.id,
                ),
                externalScope = scope,
            ).also { engine ->
                engine.start(screen.triggers)
            }
            activeScreenId = screen.id
            processPending(screen)
        } else if (screen != null) {
            processPending(screen)
        }

        val previous = previousState
        if (previous?.refreshing == true && !snapshot.refreshing) {
            triggerEngine?.onEvent(ScreenEventType.RefreshCompleted)
        }
        previousState = snapshot
    }

    private fun processPending(screen: RemoteScreen) {
        if (pendingEvents.isEmpty()) return
        val events = pendingEvents.toList()
        pendingEvents.clear()
        events.forEach { eventType -> runLifecycle(screen, eventType) }
    }

    private fun runLifecycle(screen: RemoteScreen, type: ScreenEventType) {
        val actions = when (type) {
            ScreenEventType.OnOpen -> screen.lifecycle.onOpen
            ScreenEventType.OnAppear -> screen.lifecycle.onAppear
            ScreenEventType.OnFullyVisible -> screen.lifecycle.onFullyVisible
            ScreenEventType.OnDisappear -> screen.lifecycle.onDisappear
            ScreenEventType.LoadNextPageCompleted -> emptyList()
            ScreenEventType.RefreshCompleted -> emptyList()
        }.flatMap { it.actions }

        val screenId = screen.id
        actions.forEach { action ->
            scope.launch {
                actionRegistry.dispatch(
                    action = action,
                    context = ActionContext(
                        navigator = navigator,
                        screenContext = screenContext,
                        screenId = screenId,
                    ),
                )
            }
        }
        triggerEngine?.onEvent(type)
    }

    private fun enqueueLifecycle(type: ScreenEventType) {
        pendingEvents += type
        state.value.remoteScreen?.let { screen -> processPending(screen) }
    }
}

/**
 * Factory that can be given custom ActionRegistry/VariableStore, or will create defaults.
 */
class ScreenEngineFactory(
    private val repository: ScreenRepository,
    private val navigator: Navigator,
    private val actionRegistry: ActionRegistry? = null,
    private val variableStore: VariableStore? = null,
    private val globalStore: VariableStore? = null,
    private val logger: Logger = ConsoleLogger(LogTags.ENGINE),
) {
    fun create(screenId: String, scope: CoroutineScope): ScreenEngine {
        val registry = actionRegistry ?: buildActionRegistry()
        val sharedGlobal = (globalStore ?: Companion.sharedGlobalStore)
        val localStore = variableStore ?: VariableStoreImpl(scope = scope, globalStore = sharedGlobal)
        val variables = localStore
        val controller = DefaultScreenController(
            screenId = screenId,
            repository = repository,
            navigator = navigator,
            actionRegistry = registry,
            externalScope = scope,
            providedVariableStore = variables,
            logger = logger,
        )
        return ScreenEngine(controller, registry, variables, navigator, scope, logger)
    }

    private companion object {
        val sharedGlobalStore: VariableStore by lazy {
            VariableStoreImpl(scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Default))
        }
    }
}

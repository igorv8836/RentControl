package org.igorv8836.bdui.runtime.controller

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.igorv8836.bdui.core.actions.ActionRegistry
import org.igorv8836.bdui.core.context.ActionContext
import org.igorv8836.bdui.core.context.ScreenContext
import org.igorv8836.bdui.core.navigation.Navigator
import org.igorv8836.bdui.logger.ConsoleLogger
import org.igorv8836.bdui.logger.LogMessages
import org.igorv8836.bdui.logger.LogTags
import org.igorv8836.bdui.logger.formatLog
import org.igorv8836.bdui.logger.Logger
import org.igorv8836.bdui.core.variables.VariableStore
import org.igorv8836.bdui.runtime.ScreenRepository
import org.igorv8836.bdui.runtime.ScreenState
import org.igorv8836.bdui.runtime.ScreenStore
import org.igorv8836.bdui.runtime.VariableStoreImpl

class DefaultScreenController(
    private val screenId: String,
    private val repository: ScreenRepository,
    private val navigator: Navigator,
    private val actionRegistry: ActionRegistry,
    private val externalScope: CoroutineScope,
    providedVariableStore: VariableStore? = null,
    private val logger: Logger = ConsoleLogger(LogTags.ENGINE),
) : ScreenController {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(externalScope.coroutineContext + job)
    private val store = ScreenStore(repository, scope, logger)
    override val variableStore: VariableStore = providedVariableStore ?: VariableStoreImpl(scope = externalScope)

    private val screenContext: ScreenContext = ScreenContext(
        variableStore = variableStore,
        actionRegistry = actionRegistry,
    )
    private val actionContext = ActionContext(
        navigator = navigator,
        screenContext = screenContext,
        screenId = screenId,
    )

    override val state: StateFlow<ScreenState> = store.state

    override fun onOpen() {
        store.load(screenId)
    }

    override fun onAppear() {
    }

    override fun onFullyVisible() {
    }

    override fun onDisappear() {
    }

    override fun dispose() {
        job.cancel()
    }

    fun refresh(params: Map<String, String> = emptyMap()) {
        logger.debug(formatLog(LogMessages.CONTROLLER_REFRESH, screenId, params))
        store.refresh(params = params)
    }

    fun load(screenId: String, params: Map<String, String> = emptyMap()) {
        logger.debug(formatLog(LogMessages.LOAD_START, screenId, params))
        store.load(screenId, params)
    }

    fun show(remoteScreen: org.igorv8836.bdui.contract.RemoteScreen) {
        store.show(remoteScreen)
    }

    fun loadNextPage() {
        val settings = state.value.remoteScreen?.settings?.pagination
        logger.debug(formatLog(LogMessages.CONTROLLER_NEXT_PAGE, screenId))
        store.loadNextPage(settings = settings)
    }

    fun dispatch(actionId: String) {
        val screen = state.value.remoteScreen ?: return
        val action = screen.actions.firstOrNull { it.id == actionId } ?: return
        scope.launch {
            logger.debug(formatLog(LogMessages.DISPATCH_ACTION, actionId, screen.id))
            actionRegistry.dispatch(action, actionContext)
        }
    }
}

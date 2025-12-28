package org.igorv8836.bdui.engine

import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.Json
import org.igorv8836.bdui.actions.buildActionRegistry
import org.igorv8836.bdui.actions.serialization.ActionSerializers
import org.igorv8836.bdui.cache.config.CachePolicy
import org.igorv8836.bdui.cache.store.DiskStorage
import org.igorv8836.bdui.contract.Overlay
import org.igorv8836.bdui.contract.Popup
import org.igorv8836.bdui.contract.RemoteScreen
import org.igorv8836.bdui.contract.Route
import org.igorv8836.bdui.core.actions.ActionRegistry
import org.igorv8836.bdui.core.navigation.Navigator
import org.igorv8836.bdui.core.variables.VariableStore
import org.igorv8836.bdui.logger.ConsoleLogger
import org.igorv8836.bdui.logger.Logger
import org.igorv8836.bdui.logger.LogMessages
import org.igorv8836.bdui.logger.LogTags
import org.igorv8836.bdui.logger.formatLog
import org.igorv8836.bdui.network.config.NetworkConfig
import org.igorv8836.bdui.network.datasource.KtorRemoteScreenDataSource
import org.igorv8836.bdui.network.repository.buildScreenRepository
import org.igorv8836.bdui.runtime.ScreenRepository
import org.igorv8836.bdui.runtime.VariableStoreImpl

data class EngineConfig(
    val baseUrl: String,
    val defaultHeaders: Map<String, String> = emptyMap(),
    val cachePolicy: CachePolicy = CachePolicy(),
    val diskStorage: DiskStorage? = null,
    val json: Json = Json {
        ignoreUnknownKeys = true
        classDiscriminator = "type"
        serializersModule = ActionSerializers.module
    },
    val logger: Logger = ConsoleLogger(LogTags.ENGINE),
    val onPopup: (Popup, Map<String, String>) -> Unit = { _, _ -> },
    val onOverlay: (Overlay, Map<String, String>) -> Unit = { _, _ -> },
)

/**
 * High-level builder that wires repository + engine creation.
 */
class EngineProvider(
    private val scope: CoroutineScope,
    private val config: EngineConfig,
    httpClient: HttpClient? = null,
) {
    private val repository: ScreenRepository

    init {
        val dataSource = KtorRemoteScreenDataSource(
            config = NetworkConfig(
                baseUrl = config.baseUrl,
                defaultHeaders = config.defaultHeaders,
            ),
            json = config.json,
            client = httpClient,
            logger = config.logger,
        )
        repository = buildScreenRepository(
            remote = dataSource,
            cachePolicy = config.cachePolicy,
            diskStorage = config.diskStorage,
            encode = { screen -> config.json.encodeToString(RemoteScreen.serializer(), screen) },
            decode = { body -> config.json.decodeFromString(RemoteScreen.serializer(), body) },
        )
    }

    fun engine(
        screenId: String,
        navigator: Navigator? = null,
        actionRegistry: ActionRegistry? = null,
        variableStore: VariableStore? = null,
        onNavigate: ((String) -> Unit)? = null,
    ): ScreenEngine {
        config.logger.debug(formatLog(LogMessages.ENGINE_CREATE, screenId))
        val registry = actionRegistry ?: buildActionRegistry()
        val vars = variableStore ?: VariableStoreImpl(scope = scope)
        val defaultNavigator = navigator ?: DefaultNavigator(
            onPopup = config.onPopup,
            onOverlay = config.onOverlay,
            onNavigate = onNavigate,
        )
        val factory = ScreenEngineFactory(
            repository = repository,
            navigator = defaultNavigator,
            actionRegistry = registry,
            variableStore = vars,
            logger = config.logger,
        )
        val engine = factory.create(screenId, scope)
        if (defaultNavigator is DefaultNavigator) {
            defaultNavigator.attachShow { remote -> engine.show(remote) }
            defaultNavigator.attachRouteLoader { target, params -> engine.load(target, params) }
        }
        return engine
    }
}

package org.igorv8836.bdui.network

import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.igorv8836.bdui.actions.ActionExecutor
import org.igorv8836.bdui.actions.ActionRegistry
import org.igorv8836.bdui.actions.Navigator
import org.igorv8836.bdui.actions.Router
import org.igorv8836.bdui.actions.VariableAdapter
import org.igorv8836.bdui.cache.config.CachePolicy
import org.igorv8836.bdui.cache.store.DiskStorage
import org.igorv8836.bdui.contract.Overlay
import org.igorv8836.bdui.contract.Popup
import org.igorv8836.bdui.contract.RemoteScreen
import org.igorv8836.bdui.contract.Route
import org.igorv8836.bdui.contract.PaginationSettings
import org.igorv8836.bdui.contract.VariableScope
import org.igorv8836.bdui.contract.VariableValue
import org.igorv8836.bdui.network.config.NetworkConfig
import org.igorv8836.bdui.network.datasource.KtorRemoteScreenDataSource
import org.igorv8836.bdui.network.repository.buildScreenRepository
import org.igorv8836.bdui.runtime.ScreenRepository
import org.igorv8836.bdui.runtime.ScreenState
import org.igorv8836.bdui.runtime.ScreenStore
import org.igorv8836.bdui.runtime.VariableStore

data class BduiClientConfig(
    val baseUrl: String,
    val defaultHeaders: Map<String, String> = emptyMap(),
    val cachePolicy: CachePolicy = CachePolicy(),
    val diskStorage: DiskStorage? = null,
    val json: Json = Json {
        ignoreUnknownKeys = true
        classDiscriminator = "type"
    },
    val analytics: (String, Map<String, String>) -> Unit = { _, _ -> },
    val onPopup: (Popup, Map<String, String>) -> Unit = { _, _ -> },
    val onOverlay: (Overlay, Map<String, String>) -> Unit = { _, _ -> },
    val screenIdFromPath: (String) -> String = { path -> path.trim('/') },
)

/**
    * Convenience facade to bootstrap networking, caching, variables and action handling
    * with minimal configuration (baseUrl + optional headers).
    */
class BduiClient(
    scope: CoroutineScope,
    val config: BduiClientConfig,
    httpClient: HttpClient? = null,
) {
    private val repository: ScreenRepository
    val variables = VariableStore(scope = scope)
    val store: ScreenStore
    val router: Router
    val actionRegistry: ActionRegistry

    init {
        val dataSource = KtorRemoteScreenDataSource(
            config = NetworkConfig(
                baseUrl = config.baseUrl,
                defaultHeaders = config.defaultHeaders,
            ),
            json = config.json,
            client = httpClient,
        )

        repository = buildScreenRepository(
            remote = dataSource,
            cachePolicy = config.cachePolicy,
            diskStorage = config.diskStorage,
            encode = { screen -> config.json.encodeToString(RemoteScreen.serializer(), screen) },
            decode = { body -> config.json.decodeFromString(RemoteScreen.serializer(), body) },
        )
        store = ScreenStore(repository = repository, scope = scope)

        val navigator = object : Navigator {
            override fun openRoute(route: Route, parameters: Map<String, String>) {
                load(route.destination, parameters)
            }

            override fun forward(path: String?, remoteScreen: RemoteScreen?, parameters: Map<String, String>) {
                when {
                    remoteScreen != null -> store.show(remoteScreen)
                    path != null -> load(path, parameters)
                }
            }

            override fun showPopup(popup: Popup, parameters: Map<String, String>) {
                config.onPopup(popup, parameters)
            }

            override fun showOverlay(overlay: Overlay, parameters: Map<String, String>) {
                config.onOverlay(overlay, parameters)
            }
        }

        router = object : Router {
            override fun navigate(route: Route) {
                load(route.destination)
            }
        }

        val variableAdapter = object : VariableAdapter {
            override fun peek(key: String, scope: VariableScope, screenId: String?): VariableValue? =
                variables.peek(key, scope, screenId)

            override suspend fun set(
                key: String,
                value: VariableValue,
                scope: VariableScope,
                screenId: String?,
                policy: org.igorv8836.bdui.contract.StoragePolicy,
                ttlMillis: Long?,
            ) {
                variables.set(key, value, scope, screenId, policy, ttlMillis)
            }

            override suspend fun increment(
                key: String,
                delta: Double,
                scope: VariableScope,
                screenId: String?,
                policy: org.igorv8836.bdui.contract.StoragePolicy,
            ) {
                variables.increment(key, delta, scope, screenId, policy)
            }

            override suspend fun remove(key: String, scope: VariableScope, screenId: String?) {
                variables.remove(key, scope, screenId)
            }
        }

        val executor = ActionExecutor(
            navigator = navigator,
            analytics = config.analytics,
            variables = variableAdapter,
        )
        actionRegistry = ActionRegistry(handlers = emptyMap(), executor = executor)
    }

    val state: ScreenState
        get() = store.state.value

    fun load(screenId: String, params: Map<String, String> = emptyMap()) {
        store.load(normalize(screenId), params)
    }

    fun refresh(params: Map<String, String> = emptyMap()) {
        store.refresh(params = params)
    }

    fun loadNextPage(params: Map<String, String> = emptyMap(), settings: PaginationSettings? = null) {
        store.loadNextPage(params = params, settings = settings)
    }

    private fun normalize(path: String): String = config.screenIdFromPath(path)
}

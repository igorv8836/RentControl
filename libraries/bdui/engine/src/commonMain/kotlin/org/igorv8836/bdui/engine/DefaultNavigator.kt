package org.igorv8836.bdui.engine

import org.igorv8836.bdui.contract.Overlay
import org.igorv8836.bdui.contract.Popup
import org.igorv8836.bdui.contract.RemoteScreen
import org.igorv8836.bdui.contract.Route
import org.igorv8836.bdui.core.navigation.Navigator

/**
 * Default navigator used when host does not provide its own implementation.
 * - Forward with path delegates to [onNavigate]
 * - Forward with remoteScreen renders it immediately via [showRemote]
 * - Popup/Overlay delegate to provided callbacks.
 */
internal class DefaultNavigator(
    private var showRemote: (RemoteScreen) -> Unit = {},
    private var routeLoader: (String, Map<String, String>) -> Unit = { _, _ -> },
    private val onPopup: (Popup, Map<String, String>) -> Unit,
    private val onOverlay: (Overlay, Map<String, String>) -> Unit,
    private val onNavigate: ((String) -> Unit)? = null,
) : Navigator {

    fun attachShow(show: (RemoteScreen) -> Unit) {
        showRemote = show
    }

    fun attachRouteLoader(loader: (String, Map<String, String>) -> Unit) {
        routeLoader = loader
    }

    override fun openRoute(route: Route, parameters: Map<String, String>) {
        onNavigate?.invoke(route.destination)
        routeLoader(route.destination, parameters)
    }

    override fun forward(path: String?, remoteScreen: RemoteScreen?, parameters: Map<String, String>) {
        when {
            remoteScreen != null -> showRemote(remoteScreen)
            path != null -> {
                onNavigate?.invoke(path)
                routeLoader(path, parameters)
            }
            else -> Unit
        }
    }

    override fun showPopup(popup: Popup, parameters: Map<String, String>) {
        onPopup(popup, parameters)
    }

    override fun showOverlay(overlay: Overlay, parameters: Map<String, String>) {
        onOverlay(overlay, parameters)
    }
}

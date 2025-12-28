package org.igorv8836.bdui.core.navigation

import org.igorv8836.bdui.contract.Overlay
import org.igorv8836.bdui.contract.Popup
import org.igorv8836.bdui.contract.RemoteScreen
import org.igorv8836.bdui.contract.Route

interface Navigator {
    fun openRoute(route: Route, parameters: Map<String, String> = emptyMap())
    fun forward(
        path: String? = null,
        remoteScreen: RemoteScreen? = null,
        parameters: Map<String, String> = emptyMap()
    )
    fun showPopup(popup: Popup, parameters: Map<String, String> = emptyMap())
    fun showOverlay(overlay: Overlay, parameters: Map<String, String> = emptyMap())
}
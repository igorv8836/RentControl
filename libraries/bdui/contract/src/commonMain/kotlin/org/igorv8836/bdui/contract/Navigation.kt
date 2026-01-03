package org.igorv8836.bdui.contract

import kotlinx.serialization.Serializable

@Serializable
data class Route(
    val destination: String,
    val presentation: RoutePresentation = RoutePresentation.Push,
)

@Serializable
enum class RoutePresentation {
    Push,
    Modal,
    Replace,
}

@Serializable
data class Overlay(
    val kind: OverlayKind = OverlayKind.Toast,
    val dismissible: Boolean = true,
    val payload: Map<String, String> = emptyMap(),
)

@Serializable
enum class OverlayKind {
    Toast,
    Banner,
    Fullscreen,
}

@Serializable
data class Popup(
    val style: PopupStyle = PopupStyle.Dialog,
    val position: PopupPosition = PopupPosition.Center,
    val dismissible: Boolean = true,
    val payload: Map<String, String> = emptyMap(),
)

@Serializable
enum class PopupStyle {
    Dialog,
    BottomSheet,
    Fullscreen,
}

@Serializable
enum class PopupPosition {
    Top,
    Center,
    Bottom,
}

package org.igorv8836.bdui.contract

data class Route(
    val destination: String,
    val presentation: RoutePresentation = RoutePresentation.Push,
)

enum class RoutePresentation {
    Push,
    Modal,
    Replace,
}

data class Overlay(
    val kind: OverlayKind = OverlayKind.Toast,
    val dismissible: Boolean = true,
    val payload: Map<String, String> = emptyMap(),
)

enum class OverlayKind {
    Toast,
    Banner,
    Fullscreen,
}

data class Popup(
    val style: PopupStyle = PopupStyle.Dialog,
    val position: PopupPosition = PopupPosition.Center,
    val dismissible: Boolean = true,
    val payload: Map<String, String> = emptyMap(),
)

enum class PopupStyle {
    Dialog,
    BottomSheet,
    Fullscreen,
}

enum class PopupPosition {
    Top,
    Center,
    Bottom,
}

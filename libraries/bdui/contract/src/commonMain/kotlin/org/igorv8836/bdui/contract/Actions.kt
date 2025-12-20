package org.igorv8836.bdui.contract

sealed interface Action {
    val id: String
}

data class ForwardAction(
    override val id: String,
    val path: String? = null,
    val screen: Screen? = null,
    val presentation: RoutePresentation = RoutePresentation.Push,
    val parameters: Map<String, String> = emptyMap(),
) : Action

data class OverlayAction(
    override val id: String,
    val overlay: Overlay,
    val parameters: Map<String, String> = emptyMap(),
) : Action

data class PopupAction(
    override val id: String,
    val popup: Popup,
    val parameters: Map<String, String> = emptyMap(),
) : Action

data class SubmitAction(
    override val id: String,
    val payload: Map<String, String> = emptyMap(),
) : Action

data class AnalyticsAction(
    override val id: String,
    val analytics: Analytics,
) : Action

data class CustomAction(
    override val id: String,
    val name: String,
    val parameters: Map<String, String> = emptyMap(),
) : Action

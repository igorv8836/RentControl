package org.igorv8836.bdui.contract

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface Action {
    val id: String
}

@Serializable
@SerialName("forward")
data class ForwardAction(
    override val id: String,
    val path: String? = null,
    val remoteScreen: RemoteScreen? = null,
    val presentation: RoutePresentation = RoutePresentation.Push,
    val parameters: Map<String, String> = emptyMap(),
) : Action

@Serializable
@SerialName("overlay")
data class OverlayAction(
    override val id: String,
    val overlay: Overlay,
    val parameters: Map<String, String> = emptyMap(),
) : Action

@Serializable
@SerialName("popup")
data class PopupAction(
    override val id: String,
    val popup: Popup,
    val parameters: Map<String, String> = emptyMap(),
) : Action

@Serializable
@SerialName("submit")
data class SubmitAction(
    override val id: String,
    val payload: Map<String, String> = emptyMap(),
) : Action

@Serializable
@SerialName("analytics")
data class AnalyticsAction(
    override val id: String,
    val analytics: Analytics,
) : Action

@Serializable
@SerialName("custom")
data class CustomAction(
    override val id: String,
    val name: String,
    val parameters: Map<String, String> = emptyMap(),
) : Action

@Serializable
@SerialName("setVariable")
data class SetVariableAction(
    override val id: String,
    val key: String,
    val value: VariableValue,
    val scope: VariableScope = VariableScope.Global,
    val policy: StoragePolicy = StoragePolicy.InMemory,
    val ttlMillis: Long? = null,
    val screenId: String? = null,
) : Action

@Serializable
@SerialName("incrementVariable")
data class IncrementVariableAction(
    override val id: String,
    val key: String,
    val delta: Double = 1.0,
    val scope: VariableScope = VariableScope.Global,
    val policy: StoragePolicy = StoragePolicy.InMemory,
    val screenId: String? = null,
) : Action

@Serializable
@SerialName("removeVariable")
data class RemoveVariableAction(
    override val id: String,
    val key: String,
    val scope: VariableScope = VariableScope.Global,
    val screenId: String? = null,
) : Action

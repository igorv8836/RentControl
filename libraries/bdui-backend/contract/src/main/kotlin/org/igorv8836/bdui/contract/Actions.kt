package org.igorv8836.bdui.contract

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = ForwardAction::class),
    JsonSubTypes.Type(value = OverlayAction::class),
    JsonSubTypes.Type(value = PopupAction::class),
    JsonSubTypes.Type(value = SubmitAction::class),
    JsonSubTypes.Type(value = AnalyticsAction::class),
    JsonSubTypes.Type(value = CustomAction::class),
    JsonSubTypes.Type(value = SetVariableAction::class),
    JsonSubTypes.Type(value = IncrementVariableAction::class),
    JsonSubTypes.Type(value = RemoveVariableAction::class),
    JsonSubTypes.Type(value = RemoteAction::class),
)
sealed interface Action {
    val id: String
}

data class ForwardAction(
    override val id: String,
    val path: String? = null,
    val remoteScreen: RemoteScreen? = null,
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

data class SetVariableAction(
    override val id: String,
    val key: String,
    val value: VariableValue,
    val scope: VariableScope = VariableScope.Global,
    val policy: StoragePolicy = StoragePolicy.InMemory,
    val ttlMillis: Long? = null,
    val screenId: String? = null,
) : Action

data class IncrementVariableAction(
    override val id: String,
    val key: String,
    val delta: Double = 1.0,
    val scope: VariableScope = VariableScope.Global,
    val policy: StoragePolicy = StoragePolicy.InMemory,
    val screenId: String? = null,
) : Action

data class RemoveVariableAction(
    override val id: String,
    val key: String,
    val scope: VariableScope = VariableScope.Global,
    val screenId: String? = null,
) : Action

data class RemoteAction(
    override val id: String,
    val path: String,
    val parameters: Map<String, String> = emptyMap(),
) : Action

data class ActionResponse(
    val actions: List<Action> = emptyList(),
)

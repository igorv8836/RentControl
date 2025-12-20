package org.igorv8836.bdui.contract

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = ForwardAction::class, name = "forward"),
    JsonSubTypes.Type(value = OverlayAction::class, name = "overlay"),
    JsonSubTypes.Type(value = PopupAction::class, name = "popup"),
    JsonSubTypes.Type(value = SubmitAction::class, name = "submit"),
    JsonSubTypes.Type(value = AnalyticsAction::class, name = "analytics"),
    JsonSubTypes.Type(value = CustomAction::class, name = "custom"),
    JsonSubTypes.Type(value = SetVariableAction::class, name = "setVariable"),
    JsonSubTypes.Type(value = IncrementVariableAction::class, name = "incrementVariable"),
    JsonSubTypes.Type(value = RemoveVariableAction::class, name = "removeVariable"),
)
sealed interface Action {
    val id: String
}

@JsonTypeName("forward")
data class ForwardAction(
    override val id: String,
    val path: String? = null,
    val remoteScreen: RemoteScreen? = null,
    val presentation: RoutePresentation = RoutePresentation.Push,
    val parameters: Map<String, String> = emptyMap(),
) : Action

@JsonTypeName("overlay")
data class OverlayAction(
    override val id: String,
    val overlay: Overlay,
    val parameters: Map<String, String> = emptyMap(),
) : Action

@JsonTypeName("popup")
data class PopupAction(
    override val id: String,
    val popup: Popup,
    val parameters: Map<String, String> = emptyMap(),
) : Action

@JsonTypeName("submit")
data class SubmitAction(
    override val id: String,
    val payload: Map<String, String> = emptyMap(),
) : Action

@JsonTypeName("analytics")
data class AnalyticsAction(
    override val id: String,
    val analytics: Analytics,
) : Action

@JsonTypeName("custom")
data class CustomAction(
    override val id: String,
    val name: String,
    val parameters: Map<String, String> = emptyMap(),
) : Action

@JsonTypeName("setVariable")
data class SetVariableAction(
    override val id: String,
    val key: String,
    val value: VariableValue,
    val scope: VariableScope = VariableScope.Global,
    val policy: StoragePolicy = StoragePolicy.InMemory,
    val ttlMillis: Long? = null,
    val screenId: String? = null,
) : Action

@JsonTypeName("incrementVariable")
data class IncrementVariableAction(
    override val id: String,
    val key: String,
    val delta: Double = 1.0,
    val scope: VariableScope = VariableScope.Global,
    val policy: StoragePolicy = StoragePolicy.InMemory,
    val screenId: String? = null,
) : Action

@JsonTypeName("removeVariable")
data class RemoveVariableAction(
    override val id: String,
    val key: String,
    val scope: VariableScope = VariableScope.Global,
    val screenId: String? = null,
) : Action

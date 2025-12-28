package org.igorv8836.bdui.contract

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

data class Trigger(
    val id: String,
    val source: TriggerSource,
    val condition: Condition? = null,
    val actions: List<Action> = emptyList(),
    val debounceMs: Long? = null,
    val throttleMs: Long? = null,
    val maxExecutions: Int = 10,
)

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = VariableChanged::class),
    JsonSubTypes.Type(value = ScreenEvent::class),
)
sealed interface TriggerSource

data class VariableChanged(
    val key: String,
    val scope: VariableScope = VariableScope.Global,
    val screenId: String? = null,
) : TriggerSource

data class ScreenEvent(val type: ScreenEventType) : TriggerSource

enum class ScreenEventType {
    OnOpen,
    OnAppear,
    OnFullyVisible,
    OnDisappear,
    RefreshCompleted,
    LoadNextPageCompleted,
}

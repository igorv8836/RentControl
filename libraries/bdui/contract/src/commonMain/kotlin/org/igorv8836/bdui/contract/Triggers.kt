package org.igorv8836.bdui.contract

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Trigger(
    val id: String,
    val source: TriggerSource,
    val condition: Condition? = null,
    val actions: List<@Polymorphic Action> = emptyList(),
    val debounceMs: Long? = null,
    val throttleMs: Long? = null,
    val maxExecutions: Int = 10,
)

@Serializable
sealed interface TriggerSource {
    @Serializable
    @SerialName("VariableChanged")
    data class VariableChanged(
        val key: String,
        val scope: VariableScope = VariableScope.Global,
        val screenId: String? = null,
    ) : TriggerSource

    @Serializable
    @SerialName("ScreenEvent")
    data class ScreenEvent(val type: ScreenEventType) : TriggerSource
}

@Serializable
enum class ScreenEventType {
    OnOpen,
    OnAppear,
    OnFullyVisible,
    OnDisappear,
    RefreshCompleted,
    LoadNextPageCompleted,
}

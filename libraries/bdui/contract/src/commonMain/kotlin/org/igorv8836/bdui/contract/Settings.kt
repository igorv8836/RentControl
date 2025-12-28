package org.igorv8836.bdui.contract

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable

@Serializable
data class ScreenSettings(
    val scrollable: Boolean = true,
    val pagination: PaginationSettings? = null,
    val pullToRefresh: PullToRefresh? = null,
)

@Serializable
data class PaginationSettings(
    val enabled: Boolean = false,
    val pageSize: Int? = null,
    val prefetchDistance: Int = 2,
    val cursorParam: String? = null,
    val pageParam: String? = null,
)

@Serializable
data class PullToRefresh(
    val enabled: Boolean = false,
    val actionId: String? = null,
)

@Serializable
data class ScreenLifecycle(
    val onOpen: List<UiEvent> = emptyList(),
    val onAppear: List<UiEvent> = emptyList(),
    val onFullyVisible: List<UiEvent> = emptyList(),
    val onDisappear: List<UiEvent> = emptyList(),
)

@Serializable
data class UiEvent(
    val id: String,
    val actions: List<@Polymorphic Action> = emptyList(),
)

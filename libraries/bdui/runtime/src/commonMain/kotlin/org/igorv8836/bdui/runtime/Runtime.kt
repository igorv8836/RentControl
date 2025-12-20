package org.igorv8836.bdui.runtime

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.igorv8836.bdui.contract.Container
import org.igorv8836.bdui.contract.LazyListElement
import org.igorv8836.bdui.contract.Screen

data class ScreenState(
    val screen: Screen? = null,
    val status: ScreenStatus = ScreenStatus.Idle,
    val error: String? = null,
    val refreshing: Boolean = false,
    val loadingMore: Boolean = false,
    val empty: Boolean = false,
)

enum class ScreenStatus {
    Idle,
    Loading,
    Ready,
    Error,
}

interface ScreenRepository {
    suspend fun fetch(screenId: String, params: Map<String, String> = emptyMap()): Result<Screen>
}

class ScreenStore(
    private val repository: ScreenRepository,
    private val scope: CoroutineScope,
) {
    private val stateInternal = MutableStateFlow(ScreenState())
    val state: StateFlow<ScreenState> = stateInternal

    fun load(screenId: String, params: Map<String, String> = emptyMap()) {
        stateInternal.update { it.copy(status = ScreenStatus.Loading, error = null) }
        scope.launch {
            val result = repository.fetch(screenId, params)
            stateInternal.value = result.fold(
                onSuccess = { fetched ->
                    ScreenState(
                        screen = fetched,
                        status = ScreenStatus.Ready,
                        empty = isScreenEmpty(fetched),
                    )
                },
                onFailure = { throwable ->
                    ScreenState(status = ScreenStatus.Error, error = throwable.message)
                },
            )
        }
    }

    fun refresh(screenId: String? = stateInternal.value.screen?.id, params: Map<String, String> = emptyMap()) {
        val id = screenId ?: return
        stateInternal.update { it.copy(refreshing = true, error = null) }
        scope.launch {
            val result = repository.fetch(id, params)
            stateInternal.update { prev ->
                result.fold(
                    onSuccess = { fetched ->
                        prev.copy(
                            screen = fetched,
                            status = ScreenStatus.Ready,
                            refreshing = false,
                            empty = isScreenEmpty(fetched),
                        )
                    },
                    onFailure = { throwable ->
                        prev.copy(
                            status = ScreenStatus.Error,
                            refreshing = false,
                            error = throwable.message,
                        )
                    },
                )
            }
        }
    }

    fun setLoadingMore(isLoading: Boolean) {
        stateInternal.update { it.copy(loadingMore = isLoading) }
    }

    fun setEmpty(isEmpty: Boolean) {
        stateInternal.update { it.copy(empty = isEmpty) }
    }

    private fun isScreenEmpty(screen: Screen): Boolean =
        when (val root = screen.layout.root) {
            is LazyListElement -> root.items.isEmpty()
            is Container -> root.children.all { child -> child is LazyListElement && child.items.isEmpty() }
            else -> false
        }
}

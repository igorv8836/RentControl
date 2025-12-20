package org.igorv8836.bdui.runtime

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.igorv8836.bdui.contract.Screen

data class ScreenState(
    val screen: Screen? = null,
    val status: ScreenStatus = ScreenStatus.Idle,
    val error: String? = null,
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
                    ScreenState(screen = fetched, status = ScreenStatus.Ready)
                },
                onFailure = { throwable ->
                    ScreenState(status = ScreenStatus.Error, error = throwable.message)
                },
            )
        }
    }
}

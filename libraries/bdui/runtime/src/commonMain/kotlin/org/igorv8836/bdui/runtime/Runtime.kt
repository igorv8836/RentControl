package org.igorv8836.bdui.runtime

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.igorv8836.bdui.contract.Container
import org.igorv8836.bdui.contract.LazyListElement
import org.igorv8836.bdui.contract.RemoteScreen
import org.igorv8836.bdui.contract.ComponentNode
import org.igorv8836.bdui.contract.PaginationSettings

data class ScreenState(
    val remoteScreen: RemoteScreen? = null,
    val status: ScreenStatus = ScreenStatus.Idle,
    val error: String? = null,
    val refreshing: Boolean = false,
    val loadingMore: Boolean = false,
    val empty: Boolean = false,
    val pagination: PaginationState = PaginationState(),
)

enum class ScreenStatus {
    Idle,
    Loading,
    Ready,
    Error,
}

interface ScreenRepository {
    suspend fun fetch(
        screenId: String,
        params: Map<String, String> = emptyMap(),
    ): Result<RemoteScreen>
}

data class PaginationState(
    val page: Int = 1,
    val cursor: String? = null,
    val hasMore: Boolean = true,
)

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
                        remoteScreen = fetched,
                        status = ScreenStatus.Ready,
                        empty = isScreenEmpty(fetched),
                        pagination = PaginationState(),
                    )
                },
                onFailure = { throwable ->
                    ScreenState(status = ScreenStatus.Error, error = throwable.message)
                },
            )
        }
    }

    fun refresh(screenId: String? = stateInternal.value.remoteScreen?.id, params: Map<String, String> = emptyMap()) {
        val id = screenId ?: return
        stateInternal.update { it.copy(refreshing = true, error = null, pagination = PaginationState()) }
        scope.launch {
            val result = repository.fetch(id, params)
            stateInternal.update { prev ->
                result.fold(
                    onSuccess = { fetched ->
                        prev.copy(
                            remoteScreen = fetched,
                            status = ScreenStatus.Ready,
                            refreshing = false,
                            empty = isScreenEmpty(fetched),
                            pagination = PaginationState(),
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

    fun loadNextPage(
        params: Map<String, String> = emptyMap(),
        settings: PaginationSettings? = null,
    ) {
        val snapshot = stateInternal.value
        val screen = snapshot.remoteScreen ?: return
        if (!snapshot.pagination.hasMore || snapshot.loadingMore) return
        val nextPage = snapshot.pagination.page + 1
        val mergedParams = params.toMutableMap()
        settings?.pageParam?.let { mergedParams[it] = nextPage.toString() }
        settings?.pageSize?.let { size -> mergedParams[settings.pageParam ?: "page_size"] = size.toString() }
        settings?.cursorParam?.let { key ->
            snapshot.pagination.cursor?.let { cursor -> mergedParams[key] = cursor }
        }

        stateInternal.update { it.copy(loadingMore = true, error = null) }
        scope.launch {
            val result = repository.fetch(screen.id, mergedParams)
            stateInternal.update { prev ->
                result.fold(
                    onSuccess = { fetched ->
                        val merged = mergeForPagination(prev.remoteScreen, fetched)
                        val newEmpty = isScreenEmpty(merged)
                        prev.copy(
                            remoteScreen = merged,
                            loadingMore = false,
                            empty = newEmpty,
                            pagination = prev.pagination.copy(
                                page = nextPage,
                                hasMore = !isScreenEmpty(fetched),
                            ),
                        )
                    },
                    onFailure = { throwable ->
                        prev.copy(
                            loadingMore = false,
                            status = prev.status,
                            error = throwable.message,
                        )
                    },
                )
            }
        }
    }

    private fun isScreenEmpty(remoteScreen: RemoteScreen): Boolean =
        when (val root = remoteScreen.layout.root) {
            is LazyListElement -> root.items.isEmpty()
            is Container -> root.children.all { child -> child is LazyListElement && child.items.isEmpty() }
            else -> false
        }

    private fun mergeForPagination(existing: RemoteScreen?, incoming: RemoteScreen): RemoteScreen {
        if (existing == null) return incoming
        val mergedRoot = mergeNode(existing.layout.root, incoming.layout.root)
        val mergedLayout = existing.layout.copy(
            root = mergedRoot,
            scaffold = incoming.layout.scaffold ?: existing.layout.scaffold,
        )
        return existing.copy(layout = mergedLayout)
    }

    private fun mergeNode(current: ComponentNode, incoming: ComponentNode): ComponentNode =
        when {
            current is LazyListElement && incoming is LazyListElement && current.id == incoming.id -> {
                current.copy(items = current.items + incoming.items)
            }

            current is Container && incoming is Container && current.id == incoming.id -> {
                val mergedChildren = current.children.map { child ->
                    val incomingChild = incoming.children.firstOrNull { it.id == child.id }
                    if (incomingChild != null) mergeNode(child, incomingChild) else child
                }
                current.copy(children = mergedChildren)
            }

            else -> current
        }
}

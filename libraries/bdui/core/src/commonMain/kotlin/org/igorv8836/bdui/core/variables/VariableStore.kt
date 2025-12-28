package org.igorv8836.bdui.core.variables

import kotlinx.coroutines.flow.StateFlow
import org.igorv8836.bdui.contract.StoragePolicy
import org.igorv8836.bdui.contract.VariableScope
import org.igorv8836.bdui.contract.VariableValue

interface VariableStore {
    val changes: StateFlow<Long>

    suspend fun get(key: String, scope: VariableScope, screenId: String? = null): VariableValue?

    fun peek(key: String, scope: VariableScope, screenId: String? = null): VariableValue?

    suspend fun set(
        key: String,
        value: VariableValue,
        scope: VariableScope = VariableScope.Global,
        screenId: String? = null,
        policy: StoragePolicy = StoragePolicy.InMemory,
        ttlMillis: Long? = null,
    )

    suspend fun increment(
        key: String,
        delta: Double = 1.0,
        scope: VariableScope = VariableScope.Global,
        screenId: String? = null,
        policy: StoragePolicy = StoragePolicy.InMemory,
    )

    suspend fun remove(
        key: String,
        scope: VariableScope = VariableScope.Global,
        screenId: String? = null
    )

    suspend fun syncFromPersistent(screenId: String? = null)

    fun dispose()
}
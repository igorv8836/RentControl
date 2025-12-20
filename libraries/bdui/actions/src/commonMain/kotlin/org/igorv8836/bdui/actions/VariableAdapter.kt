package org.igorv8836.bdui.actions

import org.igorv8836.bdui.contract.StoragePolicy
import org.igorv8836.bdui.contract.VariableScope
import org.igorv8836.bdui.contract.VariableValue

interface VariableAdapter {
    fun peek(key: String, scope: VariableScope = VariableScope.Global, screenId: String? = null): VariableValue?
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
        screenId: String? = null,
    )
}

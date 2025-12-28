package org.igorv8836.bdui.runtime

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.igorv8836.bdui.contract.StoragePolicy
import org.igorv8836.bdui.contract.VariableScope
import org.igorv8836.bdui.contract.VariableValue
import org.igorv8836.bdui.core.variables.VariableStore

data class StoredVariable(
    val key: String,
    val scope: VariableScope,
    val value: VariableValue,
    val updatedAtMillis: Long,
    val policy: StoragePolicy,
    val ttlMillis: Long? = null,
)

interface PersistentVariableStorage {
    suspend fun load(scope: VariableScope, screenId: String? = null): List<StoredVariable>
    suspend fun save(variable: StoredVariable, screenId: String? = null)
    suspend fun remove(key: String, scope: VariableScope, screenId: String? = null)
}

class InMemoryPersistentStorage : PersistentVariableStorage {
    private val data: MutableMap<String, StoredVariable> = mutableMapOf()

    override suspend fun load(scope: VariableScope, screenId: String?): List<StoredVariable> {
        val prefix = keyPrefix(scope, screenId)
        return data.filterKeys { it.startsWith(prefix) }.values.toList()
    }

    override suspend fun save(variable: StoredVariable, screenId: String?) {
        data[keyPrefix(variable.scope, screenId) + variable.key] = variable
    }

    override suspend fun remove(key: String, scope: VariableScope, screenId: String?) {
        data.remove(keyPrefix(scope, screenId) + key)
    }

    private fun keyPrefix(scope: VariableScope, screenId: String?): String =
        when (scope) {
            VariableScope.Global -> "global:"
            VariableScope.Screen -> "screen:${screenId ?: "unknown"}:"
        }
}

class VariableStoreImpl(
    private val persistent: PersistentVariableStorage = InMemoryPersistentStorage(),
    private val maxValueChars: Int = 4096,
    private val conflictStrategy: ConflictStrategy = ConflictStrategy.DbAsSourceOfTruth,
    private val syncIntervalMs: Long = 60_000,
    private val enableSync: Boolean = true,
    private val scope: CoroutineScope,
    private val globalStore: VariableStore? = null,
) : VariableStore {
    private val globalMemory: MutableMap<String, StoredVariable> = mutableMapOf()
    private val screenMemory: MutableMap<String, MutableMap<String, StoredVariable>> = mutableMapOf()
    private val changeSignal = MutableStateFlow(0L)
    private val syncJob: Job?

    sealed interface ConflictStrategy {
        data object DbAsSourceOfTruth : ConflictStrategy
        data object LastWriteWins : ConflictStrategy
    }

    init {
        syncJob = if (enableSync) {
            scope.launch {
                while (isActive) {
                    syncFromPersistent()
                    delay(syncIntervalMs)
                }
            }
        } else {
            null
        }
    }

    override val changes: StateFlow<Long> = changeSignal

    override suspend fun get(key: String, scope: VariableScope, screenId: String?): VariableValue? =
        peek(key, scope, screenId)

    override fun peek(key: String, scope: VariableScope, screenId: String?): VariableValue? {
        if (scope == VariableScope.Global && globalStore != null && globalStore !== this) {
            return globalStore.peek(key, scope, screenId)
        }
        evictExpired(scope, screenId)
        val stored = memoryFor(scope, screenId)[key]
        return stored?.value
    }

    override suspend fun set(
        key: String,
        value: VariableValue,
        scope: VariableScope,
        screenId: String?,
        policy: StoragePolicy,
        ttlMillis: Long?,
    ) {
        validateSize(value)
        val now = currentTimeMillis()
        if (scope == VariableScope.Global && globalStore != null && globalStore !== this) {
            globalStore.set(key, value, scope, screenId, policy, ttlMillis)
            return
        }

        val stored = StoredVariable(
            key = key,
            scope = scope,
            value = value,
            updatedAtMillis = now,
            policy = policy,
            ttlMillis = ttlMillis,
        )
        memoryFor(scope, screenId)[key] = stored
        if (policy == StoragePolicy.Persistent) {
            persistent.save(stored, screenId)
        }
        notifyChanged()
    }

    override suspend fun increment(
        key: String,
        delta: Double,
        scope: VariableScope,
        screenId: String?,
        policy: StoragePolicy,
    ) {
        if (scope == VariableScope.Global && globalStore != null && globalStore !== this) {
            globalStore.increment(key, delta, scope, screenId, policy)
            return
        }

        val current = get(key, scope, screenId)
        val newValue = when (current) {
            is VariableValue.NumberValue -> current.value + delta
            null -> delta
            else -> throw IllegalArgumentException("Variable '$key' is not a number")
        }
        set(key, VariableValue.NumberValue(newValue), scope, screenId, policy)
    }

    override suspend fun remove(key: String, scope: VariableScope, screenId: String?) {
        if (scope == VariableScope.Global && globalStore != null && globalStore !== this) {
            globalStore.remove(key, scope, screenId)
        } else {
            memoryFor(scope, screenId).remove(key)
            persistent.remove(key, scope, screenId)
            notifyChanged()
        }
    }

    override suspend fun syncFromPersistent(screenId: String?) {
        if (globalStore != null && globalStore !== this) {
            globalStore.syncFromPersistent(null)
        } else {
            syncScope(VariableScope.Global, screenId)
        }
        syncScope(VariableScope.Screen, screenId)
    }

    private suspend fun syncScope(scopeType: VariableScope, screenId: String?) {
        val loaded = persistent.load(scopeType, screenId)
        val memory = memoryFor(scopeType, screenId)
        loaded.forEach { incoming ->
            val existing = memory[incoming.key]
            val shouldReplace = when (conflictStrategy) {
                ConflictStrategy.DbAsSourceOfTruth -> true
                ConflictStrategy.LastWriteWins -> {
                    val existingTs = existing?.updatedAtMillis ?: Long.MIN_VALUE
                    incoming.updatedAtMillis >= existingTs
                }
            }
            if (shouldReplace) {
                memory[incoming.key] = incoming
            }
        }
        evictExpired(scopeType, screenId)
        notifyChanged()
    }

    private fun evictExpired(scope: VariableScope, screenId: String?) {
        val now = currentTimeMillis()
        val memory = memoryFor(scope, screenId)
        val expired = memory.filterValues { entry ->
            entry.ttlMillis?.let { ttl -> now - entry.updatedAtMillis > ttl } ?: false
        }.keys
        expired.forEach { memory.remove(it) }
    }

    private fun memoryFor(scope: VariableScope, screenId: String?): MutableMap<String, StoredVariable> =
        when (scope) {
            VariableScope.Global -> globalMemory
            VariableScope.Screen -> screenMemory.getOrPut(screenId ?: "unknown") { mutableMapOf() }
        }

    private fun validateSize(value: VariableValue) {
        val size = approximateSize(value)
        if (size > maxValueChars) {
            throw IllegalArgumentException("Variable value too large: $size > $maxValueChars characters")
        }
    }

    private fun approximateSize(value: VariableValue): Int =
        when (value) {
            is VariableValue.StringValue -> value.value.length
            is VariableValue.NumberValue -> value.value.toString().length
            is VariableValue.BoolValue -> 5
            is VariableValue.ObjectValue -> value.value.entries.sumOf { it.key.length + approximateSize(it.value) }
        }

    private fun notifyChanged() {
        changeSignal.value = changeSignal.value + 1
    }

    override fun dispose() {
        syncJob?.cancel()
    }
}

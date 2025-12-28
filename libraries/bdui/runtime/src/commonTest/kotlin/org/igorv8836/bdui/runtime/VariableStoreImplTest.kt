package org.igorv8836.bdui.runtime

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.igorv8836.bdui.contract.StoragePolicy
import org.igorv8836.bdui.contract.VariableScope
import org.igorv8836.bdui.contract.VariableValue
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class VariableStoreImplTest {

    private val dispatcher = StandardTestDispatcher()
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    @AfterTest
    fun tearDown() {
        scope.cancel()
    }

    @Test
    fun setAndGetRespectsScope() = runTest(dispatcher) {
        val store = VariableStoreImpl(enableSync = false, scope = scope)

        store.set("g", VariableValue.StringValue("global"), VariableScope.Global, null, StoragePolicy.InMemory)
        store.set("s", VariableValue.StringValue("screen"), VariableScope.Screen, "home", StoragePolicy.InMemory)

        assertEquals("global", (store.get("g", VariableScope.Global, null) as VariableValue.StringValue).value)
        assertEquals("screen", (store.get("s", VariableScope.Screen, "home") as VariableValue.StringValue).value)
        assertNull(store.get("s", VariableScope.Screen, "other"))

        store.dispose()
    }

    @Test
    fun incrementFailsForNonNumber() = runTest(dispatcher) {
        val store = VariableStoreImpl(enableSync = false, scope = scope)
        store.set("flag", VariableValue.BoolValue(true), VariableScope.Global, null, StoragePolicy.InMemory)

        assertFailsWith<IllegalArgumentException> {
            store.increment("flag", 1.0, VariableScope.Global, null, StoragePolicy.InMemory)
        }
        store.dispose()
    }

    @Test
    fun syncFromPersistentRespectsConflictStrategy() = runTest(dispatcher) {
        val persistent = object : PersistentVariableStorage {
            private var stored: List<StoredVariable> = emptyList()
            override suspend fun load(scope: VariableScope, screenId: String?): List<StoredVariable> = stored
            override suspend fun save(variable: StoredVariable, screenId: String?) { stored = listOf(variable) }
            override suspend fun remove(key: String, scope: VariableScope, screenId: String?) { stored = emptyList() }
            fun replace(vararg vars: StoredVariable) { stored = vars.toList() }
        }
        val store = VariableStoreImpl(
            persistent = persistent,
            conflictStrategy = VariableStoreImpl.ConflictStrategy.LastWriteWins,
            enableSync = false,
            scope = scope,
        )

        val older = StoredVariable(
            key = "counter",
            scope = VariableScope.Global,
            value = VariableValue.NumberValue(1.0),
            updatedAtMillis = 1L,
            policy = StoragePolicy.Persistent,
        )
        val newer = older.copy(value = VariableValue.NumberValue(2.0), updatedAtMillis = 10L)

        persistent.replace(older)
        store.syncFromPersistent()
        assertEquals(1.0, (store.get("counter", VariableScope.Global, null) as VariableValue.NumberValue).value)

        persistent.replace(newer)
        store.syncFromPersistent()
        assertEquals(2.0, (store.get("counter", VariableScope.Global, null) as VariableValue.NumberValue).value)

        store.dispose()
    }
}

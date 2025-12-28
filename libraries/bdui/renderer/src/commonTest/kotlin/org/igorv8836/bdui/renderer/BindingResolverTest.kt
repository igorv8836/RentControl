package org.igorv8836.bdui.renderer

import org.igorv8836.bdui.contract.Condition
import org.igorv8836.bdui.contract.StoragePolicy
import org.igorv8836.bdui.contract.VariableScope
import org.igorv8836.bdui.contract.VariableValue
import org.igorv8836.bdui.core.variables.VariableStore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private class MapVariableStore(
    private val values: MutableMap<String, VariableValue> = mutableMapOf(),
) : VariableStore {
    override val changes = kotlinx.coroutines.flow.MutableStateFlow(0L)
    override suspend fun get(key: String, scope: VariableScope, screenId: String?) = values[key]
    override fun peek(key: String, scope: VariableScope, screenId: String?) = values[key]
    override suspend fun set(key: String, value: VariableValue, scope: VariableScope, screenId: String?, policy: StoragePolicy, ttlMillis: Long?) {
        values[key] = value
    }
    override suspend fun increment(key: String, delta: Double, scope: VariableScope, screenId: String?, policy: org.igorv8836.bdui.contract.StoragePolicy) {}
    override suspend fun remove(key: String, scope: VariableScope, screenId: String?) { values.remove(key) }
    override suspend fun syncFromPersistent(screenId: String?) {}
    override fun dispose() {}
}

class BindingResolverTest {

    private val translate: (String) -> String = { it }

    @Test
    fun resolvesTemplatePlaceholder() {
        val store = MapVariableStore(mutableMapOf("greet" to VariableValue.StringValue("Hello")))
        val resolver = BindingResolver(store, "screen", translate)

        val result = resolver.text(
            key = null,
            template = "Value: @{greet}",
        )

        assertEquals("Value: Hello", result)
    }

    @Test
    fun interpolatesTemplateWithVariables() {
        val store = MapVariableStore(mutableMapOf("user" to VariableValue.StringValue("Guest")))
        val resolver = BindingResolver(store, "screen", translate)

        val result = resolver.text(key = "welcome", template = "Welcome, @{ user }!")

        assertEquals("Welcome, Guest!", result)
    }

    @Test
    fun visibleWhenConditionMatches() {
        val store = MapVariableStore(mutableMapOf("flag" to VariableValue.BoolValue(true)))
        val resolver = BindingResolver(store, "screen", translate)
        val condition = Condition(key = "flag", equals = VariableValue.BoolValue(true))

        assertTrue(resolver.isVisible(condition))
    }

    @Test
    fun notVisibleWhenConditionMissingAndExistsRequired() {
        val store = MapVariableStore()
        val resolver = BindingResolver(store, "screen", translate)
        val condition = Condition(key = "unknown")

        assertFalse(resolver.isVisible(condition))
    }

    @Test
    fun isEnabledRespectsBaseFlag() {
        val store = MapVariableStore()
        val resolver = BindingResolver(store, "screen", translate)

        assertFalse(resolver.isEnabled(base = false, condition = null))
    }

    @Test
    fun missingVariableReturnsEmptyString() {
        val store = MapVariableStore()
        val resolver = BindingResolver(store, "screen", translate)

        val text = resolver.text(key = null, template = "Hi @{absent}")

        assertEquals("Hi ", text)
    }
}

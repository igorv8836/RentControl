package org.igorv8836.bdui.contract

enum class VariableScope {
    Global,
    Screen,
}

enum class StoragePolicy {
    InMemory,
    Persistent,
}

sealed interface VariableValue {
    data class StringValue(val value: String) : VariableValue
    data class NumberValue(val value: Double) : VariableValue
    data class BoolValue(val value: Boolean) : VariableValue
    data class ObjectValue(val value: Map<String, VariableValue>) : VariableValue
}

enum class MissingVariableBehavior {
    Empty,
    Default,
    Error,
}

data class Binding(
    val key: String,
    val scope: VariableScope = VariableScope.Global,
    val default: VariableValue? = null,
    val missingBehavior: MissingVariableBehavior = MissingVariableBehavior.Empty,
)

data class Condition(
    val binding: Binding,
    val equals: VariableValue? = null,
    val exists: Boolean = true,
    val negate: Boolean = false,
)

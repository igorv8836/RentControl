package org.igorv8836.bdui.contract

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

enum class VariableScope {
    Global,
    Screen,
}

enum class StoragePolicy {
    InMemory,
    Persistent,
}

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = StringValue::class),
    JsonSubTypes.Type(value = NumberValue::class),
    JsonSubTypes.Type(value = BoolValue::class),
    JsonSubTypes.Type(value = ObjectValue::class),
)
sealed interface VariableValue

data class StringValue(val value: String) : VariableValue
data class NumberValue(val value: Double) : VariableValue
data class BoolValue(val value: Boolean) : VariableValue
data class ObjectValue(val value: Map<String, VariableValue>) : VariableValue

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

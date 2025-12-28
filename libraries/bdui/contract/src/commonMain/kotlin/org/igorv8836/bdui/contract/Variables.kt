package org.igorv8836.bdui.contract

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonPrimitive

@Serializable
enum class VariableScope {
    Global,
    Screen,
}

@Serializable
enum class StoragePolicy {
    InMemory,
    Persistent,
}

@Serializable(with = VariableValueSerializer::class)
sealed interface VariableValue {
    @Serializable
    data class StringValue(val value: String) : VariableValue
    @Serializable
    data class NumberValue(val value: Double) : VariableValue
    @Serializable
    data class BoolValue(val value: Boolean) : VariableValue
    @Serializable
    data class ObjectValue(val value: Map<String, VariableValue>) : VariableValue
}

@Serializable
enum class MissingVariableBehavior {
    Empty,
    Default,
    Error,
}

@Serializable
data class Binding(
    val key: String,
    val scope: VariableScope = VariableScope.Global,
    val default: VariableValue? = null,
    val missingBehavior: MissingVariableBehavior = MissingVariableBehavior.Empty,
)

@Serializable
data class Condition(
    val binding: Binding,
    val equals: VariableValue? = null,
    val exists: Boolean = true,
    val negate: Boolean = false,
)

object VariableValueSerializer : KSerializer<VariableValue> {
    override val descriptor = buildClassSerialDescriptor("VariableValue") {
        element<JsonElement>("value")
    }

    override fun serialize(encoder: Encoder, value: VariableValue) {
        if (encoder !is JsonEncoder) {
            // Fallback: encode as string
            encoder.encodeString(value.toString())
            return
        }
        when (value) {
            is VariableValue.StringValue -> encoder.encodeString(value.value)
            is VariableValue.NumberValue -> encoder.encodeDouble(value.value)
            is VariableValue.BoolValue -> encoder.encodeBoolean(value.value)
            is VariableValue.ObjectValue -> {
                val map = value.value.mapValues { (_, v) -> toJsonElement(v) }
                encoder.encodeJsonElement(JsonObject(map))
            }
        }
    }

    override fun deserialize(decoder: Decoder): VariableValue {
        val jsonDecoder = decoder as? JsonDecoder
            ?: error("VariableValueDeserializer works only with Json")
        val element = jsonDecoder.decodeJsonElement()
        return parseElement(element)
    }

    private fun parseElement(element: JsonElement): VariableValue {
        return when (element) {
            is JsonPrimitive -> when {
                element.booleanOrNull != null -> VariableValue.BoolValue(element.booleanOrNull!!)
                element.doubleOrNull != null -> VariableValue.NumberValue(element.doubleOrNull!!)
                else -> VariableValue.StringValue(element.content)
            }

            is JsonObject -> parseObject(element)
            JsonNull -> VariableValue.StringValue("")
            else -> VariableValue.StringValue(element.toString())
        }
    }

    private fun parseObject(element: JsonObject): VariableValue {
        val type = element["type"]?.jsonPrimitive?.content
        val valueNode = element["value"]
        if (type != null && valueNode != null) {
            return when (type) {
                "StringValue" -> VariableValue.StringValue(valueNode.jsonPrimitive.content)
                "NumberValue" -> VariableValue.NumberValue(valueNode.jsonPrimitive.doubleOrNull ?: 0.0)
                "BoolValue" -> VariableValue.BoolValue(valueNode.jsonPrimitive.booleanOrNull ?: false)
                "ObjectValue" -> {
                    val map = (valueNode as? JsonObject)?.mapValues { (_, v) -> parseElement(v) }.orEmpty()
                    VariableValue.ObjectValue(map)
                }
                else -> VariableValue.StringValue(valueNode.jsonPrimitive.content)
            }
        }
        if (element.size == 1 && element.containsKey("value")) {
            return parseElement(element.getValue("value"))
        }
        val map = element.mapValues { (_, v) -> parseElement(v) }
        return VariableValue.ObjectValue(map)
    }

    private fun toJsonElement(value: VariableValue): JsonElement = when (value) {
        is VariableValue.StringValue -> JsonPrimitive(value.value)
        is VariableValue.NumberValue -> JsonPrimitive(value.value)
        is VariableValue.BoolValue -> JsonPrimitive(value.value)
        is VariableValue.ObjectValue -> JsonObject(value.value.mapValues { toJsonElement(it.value) })
    }
}

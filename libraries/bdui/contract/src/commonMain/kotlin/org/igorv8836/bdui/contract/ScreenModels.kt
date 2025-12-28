package org.igorv8836.bdui.contract

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable

@Serializable
data class RemoteScreen(
    val id: String,
    val version: Int,
    val layout: Layout,
    val actions: List<@Polymorphic Action> = emptyList(),
    val triggers: List<Trigger> = emptyList(),
    val theme: Theme? = null,
    val settings: ScreenSettings = ScreenSettings(),
    val lifecycle: ScreenLifecycle = ScreenLifecycle(),
    val context: ExecutionContext = ExecutionContext(),
)

@Serializable
data class Layout(
    val root: ComponentNode,
    val scaffold: Scaffold? = null,
)

@Serializable
data class Scaffold(
    val top: ComponentNode? = null,
    val bottom: ComponentNode? = null,
)

@Serializable
data class Theme(
    val typography: Map<String, String> = emptyMap(),
    val colors: Map<String, String> = emptyMap(),
    val spacing: Map<String, Float> = emptyMap(),
)

@Serializable
data class Analytics(
    val event: String,
    val params: Map<String, String> = emptyMap(),
)

@Serializable
data class ValidationRule(
    val fieldId: String,
    val required: Boolean = false,
    val regex: String? = null,
    val message: String? = null,
)

@Serializable
data class Semantics(
    val label: String? = null,
    val hint: String? = null,
    val role: String? = null,
)

@Serializable
data class ExecutionContext(
    val screenId: String? = null,
    val parameters: Map<String, String> = emptyMap(),
    val metadata: Map<String, String> = emptyMap(),
)

package org.igorv8836.bdui.contract

data class Screen(
    val id: String,
    val version: Int,
    val layout: Layout,
    val actions: List<Action> = emptyList(),
    val triggers: List<Trigger> = emptyList(),
    val theme: Theme? = null,
    val settings: ScreenSettings = ScreenSettings(),
    val lifecycle: ScreenLifecycle = ScreenLifecycle(),
    val context: ExecutionContext = ExecutionContext(),
)

data class Layout(
    val root: ComponentNode,
)

data class Theme(
    val typography: Map<String, String> = emptyMap(),
    val colors: Map<String, String> = emptyMap(),
    val spacing: Map<String, Float> = emptyMap(),
)

data class Analytics(
    val event: String,
    val params: Map<String, String> = emptyMap(),
)

data class ValidationRule(
    val fieldId: String,
    val required: Boolean = false,
    val regex: String? = null,
    val message: String? = null,
)

data class Semantics(
    val label: String? = null,
    val hint: String? = null,
    val role: String? = null,
)

data class ExecutionContext(
    val screenId: String? = null,
    val parameters: Map<String, String> = emptyMap(),
    val metadata: Map<String, String> = emptyMap(),
)

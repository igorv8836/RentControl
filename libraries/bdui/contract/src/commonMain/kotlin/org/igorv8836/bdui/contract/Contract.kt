package org.igorv8836.bdui.contract

/**
 * Minimal contract model for backend-driven UI screens.
 * The goal is to keep the schema stable and versioned while the renderer evolves.
 */
data class Screen(
    val id: String,
    val version: Int,
    val layout: Layout,
    val actions: List<Action> = emptyList(),
    val theme: Theme? = null,
)

data class Layout(
    val root: ComponentNode,
)

sealed interface ComponentNode {
    val id: String
}

data class Container(
    override val id: String,
    val direction: ContainerDirection,
    val children: List<ComponentNode> = emptyList(),
    val spacing: Float? = null,
) : ComponentNode

enum class ContainerDirection {
    Column,
    Row,
    Overlay,
}

data class TextElement(
    override val id: String,
    val textKey: String,
    val style: TextStyle = TextStyle.Body,
    val semantics: Semantics? = null,
) : ComponentNode

enum class TextStyle {
    Title,
    Subtitle,
    Body,
    Caption,
}

data class ButtonElement(
    override val id: String,
    val titleKey: String,
    val actionId: String,
    val kind: ButtonKind = ButtonKind.Primary,
    val isEnabled: Boolean = true,
    val semantics: Semantics? = null,
) : ComponentNode

enum class ButtonKind {
    Primary,
    Secondary,
    Ghost,
}

data class ImageElement(
    override val id: String,
    val url: String,
    val description: String? = null,
) : ComponentNode

data class LazyListElement(
    override val id: String,
    val items: List<ComponentNode>,
    val placeholderCount: Int = 0,
) : ComponentNode

data class Action(
    val id: String,
    val type: ActionType,
    val route: Route? = null,
    val analytics: Analytics? = null,
)

enum class ActionType {
    Navigate,
    Submit,
    Analytics,
    Custom,
}

data class Route(
    val destination: String,
    val presentation: RoutePresentation = RoutePresentation.Push,
)

enum class RoutePresentation {
    Push,
    Modal,
    Replace,
}

data class Theme(
    val typography: Map<String, String> = emptyMap(),
    val colors: Map<String, String> = emptyMap(),
    val spacing: Map<String, Float> = emptyMap(),
)

data class Analytics(
    val event: String,
    val params: Map<String, String> = emptyMap(),
)

data class Binding(
    val key: String,
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

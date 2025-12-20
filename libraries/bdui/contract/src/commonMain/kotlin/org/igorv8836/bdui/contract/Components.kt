package org.igorv8836.bdui.contract

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

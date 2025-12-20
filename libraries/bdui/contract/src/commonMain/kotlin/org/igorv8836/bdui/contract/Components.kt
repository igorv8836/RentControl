package org.igorv8836.bdui.contract

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface ComponentNode {
    val id: String
}

@Serializable
@SerialName("container")
data class Container(
    override val id: String,
    val direction: ContainerDirection,
    val children: List<ComponentNode> = emptyList(),
    val spacing: Float? = null,
    val visibleIf: Condition? = null,
) : ComponentNode

@Serializable
enum class ContainerDirection {
    Column,
    Row,
    Overlay,
}

@Serializable
@SerialName("text")
data class TextElement(
    override val id: String,
    val textKey: String,
    val style: TextStyle = TextStyle.Body,
    val semantics: Semantics? = null,
    val binding: Binding? = null,
    val template: String? = null,
    val visibleIf: Condition? = null,
) : ComponentNode

@Serializable
enum class TextStyle {
    Title,
    Subtitle,
    Body,
    Caption,
}

@Serializable
@SerialName("button")
data class ButtonElement(
    override val id: String,
    val titleKey: String,
    val actionId: String,
    val kind: ButtonKind = ButtonKind.Primary,
    val isEnabled: Boolean = true,
    val semantics: Semantics? = null,
    val titleBinding: Binding? = null,
    val enabledIf: Condition? = null,
    val visibleIf: Condition? = null,
) : ComponentNode

@Serializable
enum class ButtonKind {
    Primary,
    Secondary,
    Ghost,
}

@Serializable
@SerialName("image")
data class ImageElement(
    override val id: String,
    val url: String,
    val description: String? = null,
    val visibleIf: Condition? = null,
) : ComponentNode

@Serializable
@SerialName("lazyList")
data class LazyListElement(
    override val id: String,
    val items: List<ComponentNode>,
    val placeholderCount: Int = 0,
    val visibleIf: Condition? = null,
) : ComponentNode

@Serializable
@SerialName("spacer")
data class SpacerElement(
    override val id: String,
    val width: Float? = null,
    val height: Float? = null,
    val visibleIf: Condition? = null,
) : ComponentNode

@Serializable
@SerialName("divider")
data class DividerElement(
    override val id: String,
    val thickness: Float? = null,
    val color: String? = null,
    val insetStart: Float? = null,
    val visibleIf: Condition? = null,
) : ComponentNode

@Serializable
@SerialName("listItem")
data class ListItemElement(
    override val id: String,
    val titleKey: String,
    val subtitleKey: String? = null,
    val actionId: String? = null,
    val semantics: Semantics? = null,
    val titleBinding: Binding? = null,
    val subtitleBinding: Binding? = null,
    val enabledIf: Condition? = null,
    val visibleIf: Condition? = null,
) : ComponentNode

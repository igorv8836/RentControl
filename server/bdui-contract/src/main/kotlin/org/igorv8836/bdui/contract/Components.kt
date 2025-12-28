package org.igorv8836.bdui.contract

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = Container::class),
    JsonSubTypes.Type(value = TextElement::class),
    JsonSubTypes.Type(value = ButtonElement::class),
    JsonSubTypes.Type(value = ImageElement::class),
    JsonSubTypes.Type(value = LazyListElement::class),
    JsonSubTypes.Type(value = SpacerElement::class),
    JsonSubTypes.Type(value = DividerElement::class),
    JsonSubTypes.Type(value = ListItemElement::class),
    JsonSubTypes.Type(value = CardElement::class),
    JsonSubTypes.Type(value = CardGridElement::class),
    JsonSubTypes.Type(value = TabsElement::class),
    JsonSubTypes.Type(value = TextFieldElement::class),
    JsonSubTypes.Type(value = DropdownElement::class),
    JsonSubTypes.Type(value = SliderElement::class),
    JsonSubTypes.Type(value = SwitchElement::class),
    JsonSubTypes.Type(value = ChipGroupElement::class),
    JsonSubTypes.Type(value = CarouselElement::class),
    JsonSubTypes.Type(value = ModalElement::class),
    JsonSubTypes.Type(value = SnackbarElement::class),
    JsonSubTypes.Type(value = StateElement::class),
    JsonSubTypes.Type(value = ProgressElement::class),
    JsonSubTypes.Type(value = MapElement::class),
)
sealed interface ComponentNode {
    val id: String
}

data class Container(
    override val id: String,
    val direction: ContainerDirection,
    val children: List<ComponentNode> = emptyList(),
    val spacing: Float? = null,
    val visibleIf: Condition? = null,
) : ComponentNode

enum class ContainerDirection {
    Column,
    Row,
    Overlay,
}

data class TextElement(
    override val id: String,
    val text: String,
    val style: TextStyle = TextStyle.Body,
    val semantics: Semantics? = null,
    val template: String? = null,
    val visibleIf: Condition? = null,
) : ComponentNode

enum class TextStyle {
    Title,
    Subtitle,
    Body,
    Caption,
}

data class ButtonElement(
    override val id: String,
    val title: String,
    val actionId: String,
    val kind: ButtonKind = ButtonKind.Primary,
    val isEnabled: Boolean = true,
    val semantics: Semantics? = null,
    val enabledIf: Condition? = null,
    val visibleIf: Condition? = null,
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
    val visibleIf: Condition? = null,
) : ComponentNode

data class LazyListElement(
    override val id: String,
    val items: List<ComponentNode>,
    val placeholderCount: Int = 0,
    val visibleIf: Condition? = null,
) : ComponentNode

data class SpacerElement(
    override val id: String,
    val width: Float? = null,
    val height: Float? = null,
    val visibleIf: Condition? = null,
) : ComponentNode

data class DividerElement(
    override val id: String,
    val thickness: Float? = null,
    val color: String? = null,
    val insetStart: Float? = null,
    val visibleIf: Condition? = null,
) : ComponentNode

data class ListItemElement(
    override val id: String,
    val title: String,
    val subtitle: String? = null,
    val actionId: String? = null,
    val semantics: Semantics? = null,
    val enabledIf: Condition? = null,
    val visibleIf: Condition? = null,
) : ComponentNode

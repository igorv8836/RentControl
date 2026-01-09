package org.igorv8836.bdui.contract

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface ComponentNode {
    val id: String
}

@Serializable
data class Color(
    val light: String,
    val dark: String? = null,
)

@Serializable
@SerialName("Container")
data class Container(
    override val id: String,
    val direction: ContainerDirection,
    val children: List<ComponentNode> = emptyList(),
    val spacing: Float? = null,
    val backgroundColor: Color? = null,
    val visibleIf: Condition? = null,
) : ComponentNode

@Serializable
enum class ContainerDirection {
    Column,
    Row,
    Overlay,
}

@Serializable
@SerialName("TextElement")
data class TextElement(
    override val id: String,
    val text: String,
    val style: TextStyle = TextStyle.Body,
    val textColor: Color? = null,
    val semantics: Semantics? = null,
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
@SerialName("ButtonElement")
data class ButtonElement(
    override val id: String,
    val title: String,
    val actionId: String,
    val kind: ButtonKind = ButtonKind.Primary,
    val isEnabled: Boolean = true,
    val textColor: Color? = null,
    val backgroundColor: Color? = null,
    val semantics: Semantics? = null,
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
@SerialName("ImageElement")
data class ImageElement(
    override val id: String,
    val url: String,
    val description: String? = null,
    val backgroundColor: Color? = null,
    val textColor: Color? = null,
    val visibleIf: Condition? = null,
) : ComponentNode

@Serializable
@SerialName("LazyListElement")
data class LazyListElement(
    override val id: String,
    val items: List<ComponentNode>,
    val placeholderCount: Int = 0,
    val backgroundColor: Color? = null,
    val visibleIf: Condition? = null,
) : ComponentNode

@Serializable
@SerialName("SpacerElement")
data class SpacerElement(
    override val id: String,
    val width: Float? = null,
    val height: Float? = null,
    val visibleIf: Condition? = null,
) : ComponentNode

@Serializable
@SerialName("DividerElement")
data class DividerElement(
    override val id: String,
    val thickness: Float? = null,
    val color: Color? = null,
    val insetStart: Float? = null,
    val visibleIf: Condition? = null,
) : ComponentNode

@Serializable
@SerialName("ListItemElement")
data class ListItemElement(
    override val id: String,
    val title: String,
    val subtitle: String? = null,
    val actionId: String? = null,
    val titleColor: Color? = null,
    val subtitleColor: Color? = null,
    val backgroundColor: Color? = null,
    val semantics: Semantics? = null,
    val enabledIf: Condition? = null,
    val visibleIf: Condition? = null,
) : ComponentNode

@Serializable
@SerialName("CardElement")
data class CardElement(
    override val id: String,
    val title: String,
    val subtitle: String? = null,
    val imageUrl: String? = null,
    val badge: String? = null,
    val actionId: String? = null,
    val titleColor: Color? = null,
    val subtitleColor: Color? = null,
    val badgeTextColor: Color? = null,
    val badgeBackgroundColor: Color? = null,
    val backgroundColor: Color? = null,
    val visibleIf: Condition? = null,
) : ComponentNode

@Serializable
@SerialName("CardGridElement")
data class CardGridElement(
    override val id: String,
    val items: List<CardElement>,
    val columns: Int = 2,
    val backgroundColor: Color? = null,
    val visibleIf: Condition? = null,
) : ComponentNode

@Serializable
@SerialName("TabsElement")
data class TabsElement(
    override val id: String,
    val tabs: List<TabItem>,
    val selectedTabId: String? = null,
    val selectedTabTextColor: Color? = null,
    val unselectedTabTextColor: Color? = null,
    val selectedTabBackgroundColor: Color? = null,
    val unselectedTabBackgroundColor: Color? = null,
    val visibleIf: Condition? = null,
) : ComponentNode

@Serializable
data class TabItem(
    val id: String,
    val title: String,
    val actionId: String,
    val badge: String? = null,
    val textColor: Color? = null,
    val selectedTextColor: Color? = null,
    val backgroundColor: Color? = null,
    val selectedBackgroundColor: Color? = null,
    val badgeTextColor: Color? = null,
    val badgeBackgroundColor: Color? = null,
    val visibleIf: Condition? = null,
)

@Serializable
@SerialName("TextFieldElement")
data class TextFieldElement(
    override val id: String,
    val label: String,
    val value: String = "",
    val placeholder: String? = null,
    val actionId: String? = null,
    val textColor: Color? = null,
    val labelColor: Color? = null,
    val placeholderColor: Color? = null,
    val backgroundColor: Color? = null,
    val visibleIf: Condition? = null,
) : ComponentNode

@Serializable
@SerialName("DropdownElement")
data class DropdownElement(
    override val id: String,
    val label: String,
    val options: List<String>,
    val selectedIndex: Int? = null,
    val actionId: String? = null,
    val labelColor: Color? = null,
    val selectedTextColor: Color? = null,
    val backgroundColor: Color? = null,
    val visibleIf: Condition? = null,
) : ComponentNode

@Serializable
@SerialName("SliderElement")
data class SliderElement(
    override val id: String,
    val value: Float = 0f,
    val rangeStart: Float = 0f,
    val rangeEnd: Float = 1f,
    val actionId: String? = null,
    val textColor: Color? = null,
    val thumbColor: Color? = null,
    val activeTrackColor: Color? = null,
    val inactiveTrackColor: Color? = null,
    val visibleIf: Condition? = null,
) : ComponentNode

@Serializable
@SerialName("SwitchElement")
data class SwitchElement(
    override val id: String,
    val checked: Boolean = false,
    val title: String,
    val actionId: String? = null,
    val titleColor: Color? = null,
    val checkedThumbColor: Color? = null,
    val uncheckedThumbColor: Color? = null,
    val checkedTrackColor: Color? = null,
    val uncheckedTrackColor: Color? = null,
    val visibleIf: Condition? = null,
) : ComponentNode

@Serializable
@SerialName("ChipGroupElement")
data class ChipGroupElement(
    override val id: String,
    val chips: List<ChipItem>,
    val singleSelection: Boolean = true,
    val chipTextColor: Color? = null,
    val chipBackgroundColor: Color? = null,
    val selectedChipTextColor: Color? = null,
    val selectedChipBackgroundColor: Color? = null,
    val visibleIf: Condition? = null,
) : ComponentNode

@Serializable
data class ChipItem(
    val id: String,
    val label: String,
    val selected: Boolean = false,
    val actionId: String? = null,
    val textColor: Color? = null,
    val backgroundColor: Color? = null,
    val selectedTextColor: Color? = null,
    val selectedBackgroundColor: Color? = null,
    val visibleIf: Condition? = null,
)

@Serializable
@SerialName("CarouselElement")
data class CarouselElement(
    override val id: String,
    val items: List<ComponentNode>,
    val backgroundColor: Color? = null,
    val visibleIf: Condition? = null,
) : ComponentNode

@Serializable
@SerialName("ModalElement")
data class ModalElement(
    override val id: String,
    val content: ComponentNode,
    val primaryActionId: String? = null,
    val dismissActionId: String? = null,
    val backgroundColor: Color? = null,
    val scrimColor: Color? = null,
    val visibleIf: Condition? = null,
) : ComponentNode

@Serializable
@SerialName("SnackbarElement")
data class SnackbarElement(
    override val id: String,
    val message: String,
    val actionText: String? = null,
    val actionId: String? = null,
    val messageColor: Color? = null,
    val backgroundColor: Color? = null,
    val actionTextColor: Color? = null,
    val visibleIf: Condition? = null,
) : ComponentNode

@Serializable
@SerialName("StateElement")
data class StateElement(
    override val id: String,
    val state: StateKind,
    val message: String? = null,
    val actionId: String? = null,
    val textColor: Color? = null,
    val backgroundColor: Color? = null,
    val actionTextColor: Color? = null,
    val visibleIf: Condition? = null,
) : ComponentNode

@Serializable
enum class StateKind { Loading, Empty, Error, Success }

@Serializable
@SerialName("ProgressElement")
data class ProgressElement(
    override val id: String,
    val style: ProgressStyle = ProgressStyle.Linear,
    val progress: Float? = null,
    val indicatorColor: Color? = null,
    val trackColor: Color? = null,
    val visibleIf: Condition? = null,
) : ComponentNode

@Serializable
enum class ProgressStyle { Linear, Circular }

@Serializable
@SerialName("MapElement")
data class MapElement(
    override val id: String,
    val title: String? = null,
    val subtitle: String? = null,
    val titleColor: Color? = null,
    val subtitleColor: Color? = null,
    val backgroundColor: Color? = null,
    val visibleIf: Condition? = null,
) : ComponentNode

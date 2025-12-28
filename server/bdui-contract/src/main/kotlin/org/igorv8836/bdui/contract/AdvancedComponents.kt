package org.igorv8836.bdui.contract

import com.fasterxml.jackson.annotation.JsonTypeName

@JsonTypeName("CardElement")
data class CardElement(
    override val id: String,
    val title: String,
    val subtitle: String? = null,
    val imageUrl: String? = null,
    val badge: String? = null,
    val actionId: String? = null,
    val visibleIf: Condition? = null,
) : ComponentNode

@JsonTypeName("CardGridElement")
data class CardGridElement(
    override val id: String,
    val items: List<CardElement>,
    val columns: Int = 2,
    val visibleIf: Condition? = null,
) : ComponentNode

@JsonTypeName("TabsElement")
data class TabsElement(
    override val id: String,
    val tabs: List<TabItem>,
    val selectedTabId: String? = null,
    val visibleIf: Condition? = null,
) : ComponentNode

data class TabItem(
    val id: String,
    val title: String,
    val actionId: String,
    val badge: String? = null,
    val visibleIf: Condition? = null,
)

@JsonTypeName("TextFieldElement")
data class TextFieldElement(
    override val id: String,
    val label: String,
    val value: String = "",
    val placeholder: String? = null,
    val actionId: String? = null,
    val visibleIf: Condition? = null,
) : ComponentNode

@JsonTypeName("DropdownElement")
data class DropdownElement(
    override val id: String,
    val label: String,
    val options: List<String>,
    val selectedIndex: Int? = null,
    val actionId: String? = null,
    val visibleIf: Condition? = null,
) : ComponentNode

@JsonTypeName("SliderElement")
data class SliderElement(
    override val id: String,
    val value: Float = 0f,
    val rangeStart: Float = 0f,
    val rangeEnd: Float = 1f,
    val actionId: String? = null,
    val visibleIf: Condition? = null,
) : ComponentNode

@JsonTypeName("SwitchElement")
data class SwitchElement(
    override val id: String,
    val checked: Boolean = false,
    val title: String,
    val actionId: String? = null,
    val visibleIf: Condition? = null,
) : ComponentNode

@JsonTypeName("ChipGroupElement")
data class ChipGroupElement(
    override val id: String,
    val chips: List<ChipItem>,
    val singleSelection: Boolean = true,
    val visibleIf: Condition? = null,
) : ComponentNode

data class ChipItem(
    val id: String,
    val label: String,
    val selected: Boolean = false,
    val actionId: String? = null,
    val visibleIf: Condition? = null,
)

@JsonTypeName("CarouselElement")
data class CarouselElement(
    override val id: String,
    val items: List<ComponentNode>,
    val visibleIf: Condition? = null,
) : ComponentNode

@JsonTypeName("ModalElement")
data class ModalElement(
    override val id: String,
    val content: ComponentNode,
    val primaryActionId: String? = null,
    val dismissActionId: String? = null,
    val visibleIf: Condition? = null,
) : ComponentNode

@JsonTypeName("SnackbarElement")
data class SnackbarElement(
    override val id: String,
    val message: String,
    val actionText: String? = null,
    val actionId: String? = null,
    val visibleIf: Condition? = null,
) : ComponentNode

@JsonTypeName("StateElement")
data class StateElement(
    override val id: String,
    val state: StateKind,
    val message: String? = null,
    val actionId: String? = null,
    val visibleIf: Condition? = null,
) : ComponentNode

enum class StateKind { Loading, Empty, Error, Success }

@JsonTypeName("ProgressElement")
data class ProgressElement(
    override val id: String,
    val style: ProgressStyle = ProgressStyle.Linear,
    val progress: Float? = null,
    val visibleIf: Condition? = null,
) : ComponentNode

enum class ProgressStyle { Linear, Circular }

@JsonTypeName("MapElement")
data class MapElement(
    override val id: String,
    val title: String? = null,
    val subtitle: String? = null,
    val visibleIf: Condition? = null,
) : ComponentNode

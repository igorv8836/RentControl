package org.igorv8836.bdui.backend.dsl

import org.igorv8836.bdui.contract.Action
import org.igorv8836.bdui.contract.Color
import org.igorv8836.bdui.contract.Condition
import org.igorv8836.bdui.contract.DropdownElement
import org.igorv8836.bdui.contract.SliderElement
import org.igorv8836.bdui.contract.SwitchElement
import org.igorv8836.bdui.contract.TextFieldElement

fun ContainerScope.textField(
    id: String,
    label: String,
    value: String = "",
    placeholder: String? = null,
    action: Action? = null,
    textColor: Color? = null,
    labelColor: Color? = null,
    placeholderColor: Color? = null,
    backgroundColor: Color? = null,
    visibleIf: Condition? = null,
): TextFieldElement {
    val actionId = action?.id
    action?.let { ctx.register(it) }

    return TextFieldElement(
        id = id,
        label = label,
        value = value,
        placeholder = placeholder,
        actionId = actionId,
        textColor = textColor,
        labelColor = labelColor,
        placeholderColor = placeholderColor,
        backgroundColor = backgroundColor,
        visibleIf = visibleIf,
    ).also { children += it }
}

fun ContainerScope.dropdown(
    id: String,
    label: String,
    options: List<String>,
    selectedIndex: Int? = null,
    action: Action? = null,
    labelColor: Color? = null,
    selectedTextColor: Color? = null,
    backgroundColor: Color? = null,
    visibleIf: Condition? = null,
): DropdownElement {
    val actionId = action?.id
    action?.let { ctx.register(it) }

    return DropdownElement(
        id = id,
        label = label,
        options = options,
        selectedIndex = selectedIndex,
        actionId = actionId,
        labelColor = labelColor,
        selectedTextColor = selectedTextColor,
        backgroundColor = backgroundColor,
        visibleIf = visibleIf,
    ).also { children += it }
}

fun ContainerScope.slider(
    id: String,
    value: Float = 0f,
    rangeStart: Float = 0f,
    rangeEnd: Float = 1f,
    action: Action? = null,
    textColor: Color? = null,
    thumbColor: Color? = null,
    activeTrackColor: Color? = null,
    inactiveTrackColor: Color? = null,
    visibleIf: Condition? = null,
): SliderElement {
    val actionId = action?.id
    action?.let { ctx.register(it) }

    return SliderElement(
        id = id,
        value = value,
        rangeStart = rangeStart,
        rangeEnd = rangeEnd,
        actionId = actionId,
        textColor = textColor,
        thumbColor = thumbColor,
        activeTrackColor = activeTrackColor,
        inactiveTrackColor = inactiveTrackColor,
        visibleIf = visibleIf,
    ).also { children += it }
}

fun ContainerScope.switch(
    id: String,
    title: String,
    checked: Boolean = false,
    action: Action? = null,
    titleColor: Color? = null,
    checkedThumbColor: Color? = null,
    uncheckedThumbColor: Color? = null,
    checkedTrackColor: Color? = null,
    uncheckedTrackColor: Color? = null,
    visibleIf: Condition? = null,
): SwitchElement {
    val actionId = action?.id
    action?.let { ctx.register(it) }

    return SwitchElement(
        id = id,
        checked = checked,
        title = title,
        actionId = actionId,
        titleColor = titleColor,
        checkedThumbColor = checkedThumbColor,
        uncheckedThumbColor = uncheckedThumbColor,
        checkedTrackColor = checkedTrackColor,
        uncheckedTrackColor = uncheckedTrackColor,
        visibleIf = visibleIf,
    ).also { children += it }
}


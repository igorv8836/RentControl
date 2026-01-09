package org.igorv8836.bdui.backend.dsl

import org.igorv8836.bdui.contract.Action
import org.igorv8836.bdui.contract.Color
import org.igorv8836.bdui.contract.Condition
import org.igorv8836.bdui.contract.ProgressElement
import org.igorv8836.bdui.contract.ProgressStyle
import org.igorv8836.bdui.contract.SnackbarElement
import org.igorv8836.bdui.contract.StateElement
import org.igorv8836.bdui.contract.StateKind

fun ContainerScope.snackbar(
    id: String,
    message: String,
    actionText: String? = null,
    action: Action? = null,
    messageColor: Color? = null,
    backgroundColor: Color? = null,
    actionTextColor: Color? = null,
    visibleIf: Condition? = null,
): SnackbarElement {
    val actionId = action?.id
    action?.let { ctx.register(it) }

    return SnackbarElement(
        id = id,
        message = message,
        actionText = actionText,
        actionId = actionId,
        messageColor = messageColor,
        backgroundColor = backgroundColor,
        actionTextColor = actionTextColor,
        visibleIf = visibleIf,
    ).also { children += it }
}

fun ContainerScope.state(
    id: String,
    state: StateKind,
    message: String? = null,
    action: Action? = null,
    textColor: Color? = null,
    backgroundColor: Color? = null,
    actionTextColor: Color? = null,
    visibleIf: Condition? = null,
): StateElement {
    val actionId = action?.id
    action?.let { ctx.register(it) }

    return StateElement(
        id = id,
        state = state,
        message = message,
        actionId = actionId,
        textColor = textColor,
        backgroundColor = backgroundColor,
        actionTextColor = actionTextColor,
        visibleIf = visibleIf,
    ).also { children += it }
}

fun ContainerScope.progress(
    id: String,
    style: ProgressStyle = ProgressStyle.Linear,
    progress: Float? = null,
    indicatorColor: Color? = null,
    trackColor: Color? = null,
    visibleIf: Condition? = null,
): ProgressElement = ProgressElement(
    id = id,
    style = style,
    progress = progress,
    indicatorColor = indicatorColor,
    trackColor = trackColor,
    visibleIf = visibleIf,
).also { children += it }


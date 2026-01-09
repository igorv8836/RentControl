package org.igorv8836.bdui.backend.dsl

import org.igorv8836.bdui.backend.core.RenderContext
import org.igorv8836.bdui.contract.Action
import org.igorv8836.bdui.contract.ChipGroupElement
import org.igorv8836.bdui.contract.ChipItem
import org.igorv8836.bdui.contract.Color
import org.igorv8836.bdui.contract.Condition

class ChipGroupScope internal constructor(
    private val ctx: RenderContext,
) {
    internal val chips = mutableListOf<ChipItem>()

    fun chip(
        id: String,
        label: String,
        selected: Boolean = false,
        action: Action? = null,
        textColor: Color? = null,
        backgroundColor: Color? = null,
        selectedTextColor: Color? = null,
        selectedBackgroundColor: Color? = null,
        visibleIf: Condition? = null,
    ) {
        val actionId = action?.id
        action?.let { ctx.register(it) }
        chips += ChipItem(
            id = id,
            label = label,
            selected = selected,
            actionId = actionId,
            textColor = textColor,
            backgroundColor = backgroundColor,
            selectedTextColor = selectedTextColor,
            selectedBackgroundColor = selectedBackgroundColor,
            visibleIf = visibleIf,
        )
    }
}

fun ContainerScope.chipGroup(
    id: String,
    singleSelection: Boolean = true,
    chipTextColor: Color? = null,
    chipBackgroundColor: Color? = null,
    selectedChipTextColor: Color? = null,
    selectedChipBackgroundColor: Color? = null,
    visibleIf: Condition? = null,
    block: ChipGroupScope.() -> Unit,
): ChipGroupElement {
    val scope = ChipGroupScope(ctx).apply(block)
    return ChipGroupElement(
        id = id,
        chips = scope.chips.toList(),
        singleSelection = singleSelection,
        chipTextColor = chipTextColor,
        chipBackgroundColor = chipBackgroundColor,
        selectedChipTextColor = selectedChipTextColor,
        selectedChipBackgroundColor = selectedChipBackgroundColor,
        visibleIf = visibleIf,
    ).also { children += it }
}


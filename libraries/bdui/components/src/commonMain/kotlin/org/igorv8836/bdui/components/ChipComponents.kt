package org.igorv8836.bdui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.igorv8836.bdui.contract.ChipGroupElement
import org.igorv8836.bdui.contract.ChipItem

@Composable
fun ChipGroupComponent(
    node: ChipGroupElement,
    modifier: Modifier = Modifier,
    onAction: (String?) -> Unit = {},
) {
    val selected = remember(node.id) {
        mutableStateListOf<String>().apply {
            addAll(node.chips.filter { it.selected }.map { it.id })
        }
    }
    Row(
        modifier = modifier.wrapContentWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        node.chips.forEach { chip ->
            AssistChip(
                onClick = {
                    if (node.singleSelection) {
                        selected.clear()
                        selected.add(chip.id)
                    } else {
                        if (selected.contains(chip.id)) selected.remove(chip.id) else selected.add(chip.id)
                    }
                    chip.actionId?.let(onAction)
                },
                label = { Text(chip.label) },
                leadingIcon = null,
                modifier = Modifier,
            )
        }
    }
}

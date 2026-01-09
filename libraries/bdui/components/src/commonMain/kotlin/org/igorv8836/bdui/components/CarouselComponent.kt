package org.igorv8836.bdui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.igorv8836.bdui.contract.ComponentNode
import org.igorv8836.bdui.contract.CarouselElement

@Composable
fun CarouselComponent(
    node: CarouselElement,
    renderChild: @Composable (ComponentNode) -> Unit,
    backgroundColor: Color? = null,
    modifier: Modifier = Modifier,
) {
    val appliedModifier = backgroundColor?.let { modifier.background(it) } ?: modifier
    LazyRow(modifier = appliedModifier.fillMaxWidth()) {
        itemsIndexed(node.items) { _, item ->
            renderChild(item)
        }
    }
}

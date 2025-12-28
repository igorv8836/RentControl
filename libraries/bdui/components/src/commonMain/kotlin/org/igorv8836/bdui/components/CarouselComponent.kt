package org.igorv8836.bdui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.igorv8836.bdui.contract.ComponentNode
import org.igorv8836.bdui.contract.CarouselElement

@Composable
fun CarouselComponent(
    node: CarouselElement,
    renderChild: @Composable (ComponentNode) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(modifier = modifier.fillMaxWidth()) {
        itemsIndexed(node.items) { _, item ->
            renderChild(item)
        }
    }
}

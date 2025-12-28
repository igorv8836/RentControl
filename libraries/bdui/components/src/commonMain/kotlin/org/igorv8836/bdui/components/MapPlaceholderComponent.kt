package org.igorv8836.bdui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.igorv8836.bdui.contract.MapElement

@Composable
fun MapPlaceholderComponent(
    node: MapElement,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth().height(180.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Box {
            Text(
                text = node.title ?: "Map placeholder",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
            )
        }
    }
}

package org.igorv8836.bdui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.igorv8836.bdui.contract.MapElement

@Composable
fun MapPlaceholderComponent(
    node: MapElement,
    modifier: Modifier = Modifier,
    titleColor: Color? = null,
    subtitleColor: Color? = null,
    backgroundColor: Color? = null,
) {
    Surface(
        modifier = modifier.fillMaxWidth().height(180.dp),
        color = backgroundColor ?: MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = node.title ?: "Map placeholder",
                color = titleColor ?: MaterialTheme.colorScheme.onSurfaceVariant,
            )
            node.subtitle?.let { subtitle ->
                Text(
                    text = subtitle,
                    color = subtitleColor ?: MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

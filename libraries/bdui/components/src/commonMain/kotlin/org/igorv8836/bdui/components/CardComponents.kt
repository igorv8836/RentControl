package org.igorv8836.bdui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.igorv8836.bdui.contract.CardElement
import org.igorv8836.bdui.contract.CardGridElement

@Composable
fun CardGridComponent(
    node: CardGridElement,
    renderCard: @Composable (CardElement) -> Unit,
    backgroundColor: Color? = null,
    modifier: Modifier = Modifier,
) {
    val cols = node.columns.coerceAtLeast(1)
    Column(
        modifier = backgroundColor?.let { modifier.background(it) } ?: modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        node.items.chunked(cols).forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                row.forEach { card ->
                    Column(modifier = Modifier.weight(1f, fill = true)) {
                        renderCard(card)
                    }
                }
                repeat((cols - row.size).coerceAtLeast(0)) {
                    Column(modifier = Modifier.weight(1f, fill = true)) {}
                }
            }
        }
    }
}

@Composable
fun CardComponent(
    node: CardElement,
    onAction: ((String) -> Unit)? = null,
    titleColor: Color? = null,
    subtitleColor: Color? = null,
    badgeTextColor: Color? = null,
    badgeBackgroundColor: Color? = null,
    backgroundColor: Color? = null,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = { node.actionId?.let { onAction?.invoke(it) } },
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 1.dp,
        color = backgroundColor ?: MaterialTheme.colorScheme.surface,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            node.badge?.let { badgeText ->
                Surface(
                    shape = RoundedCornerShape(50),
                    color = badgeBackgroundColor ?: MaterialTheme.colorScheme.secondaryContainer,
                ) {
                    Text(
                        text = badgeText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        color = badgeTextColor ?: MaterialTheme.colorScheme.onSecondaryContainer,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
            Text(
                text = node.title,
                color = titleColor ?: Color.Unspecified,
                style = MaterialTheme.typography.titleMedium,
            )
            node.subtitle?.let { subtitle ->
                Text(
                    text = subtitle,
                    color = subtitleColor ?: MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

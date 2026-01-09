package org.igorv8836.bdui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Badge
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.igorv8836.bdui.contract.TabItem
import org.igorv8836.bdui.contract.TabsElement

@Composable
fun TabsComponent(
    node: TabsElement,
    modifier: Modifier = Modifier,
    onAction: (String) -> Unit,
    resolveTabColors: (TabItem, Boolean) -> TabVisualStyle,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val selected = node.selectedTabId
        node.tabs.forEach { tab ->
            val selectedStyle = resolveTabColors(tab, true)
            val unselectedStyle = resolveTabColors(tab, false)
            val colors = FilterChipDefaults.filterChipColors(
                containerColor = unselectedStyle.containerColor ?: MaterialTheme.colorScheme.surfaceVariant,
                labelColor = unselectedStyle.labelColor ?: MaterialTheme.colorScheme.onSurfaceVariant,
                selectedContainerColor = selectedStyle.containerColor ?: MaterialTheme.colorScheme.primaryContainer,
                selectedLabelColor = selectedStyle.labelColor ?: MaterialTheme.colorScheme.onPrimaryContainer,
            )
            val badgeText = tab.badge
            val badgeStyle = if (selected == tab.id) selectedStyle else unselectedStyle
            FilterChip(
                selected = selected == tab.id,
                onClick = { onAction(tab.actionId) },
                colors = colors,
                label = {
                    Text(
                        text = tab.title,
                        color = Color.Unspecified,
                    )
                },
                leadingIcon = badgeText?.let { text ->
                    {
                        val containerColor = badgeStyle.badgeContainerColor
                        val contentColor = badgeStyle.badgeContentColor
                        if (containerColor == null && contentColor == null) {
                            Badge { Text(text) }
                        } else {
                            Badge(
                                containerColor = containerColor ?: MaterialTheme.colorScheme.error,
                                contentColor = contentColor ?: MaterialTheme.colorScheme.onError,
                            ) {
                                Text(text = text, color = Color.Unspecified)
                            }
                        }
                    }
                },
            )
        }
    }
}

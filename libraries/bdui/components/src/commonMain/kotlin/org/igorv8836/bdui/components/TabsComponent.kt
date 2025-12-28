package org.igorv8836.bdui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.igorv8836.bdui.contract.TabItem
import org.igorv8836.bdui.contract.TabsElement

@Composable
fun TabsComponent(
    node: TabsElement,
    modifier: Modifier = Modifier,
    onAction: (String) -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val selected = node.selectedTabId
        node.tabs.forEach { tab ->
            FilterChip(
                selected = selected == tab.id,
                onClick = { onAction(tab.actionId) },
                label = { Text(tab.title) },
                leadingIcon = tab.badge?.let { badgeText ->
                    {
                        BadgedBox(badge = { Badge { Text(badgeText) } }) { }
                    }
                },
            )
        }
    }
}

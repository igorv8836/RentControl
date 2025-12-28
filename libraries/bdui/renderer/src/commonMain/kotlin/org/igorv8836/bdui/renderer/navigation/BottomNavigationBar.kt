package org.igorv8836.bdui.renderer.navigation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.igorv8836.bdui.contract.BottomBar
import org.igorv8836.bdui.contract.BottomTab
import org.igorv8836.bdui.renderer.binding.BindingResolver
import org.igorv8836.bdui.renderer.node.RenderNode

@Composable
internal fun BottomNavigationBar(
    bottomBar: BottomBar,
    resolver: BindingResolver,
    selectedTabId: String?,
    onTabSelected: (BottomTab) -> Unit,
) {
    val visibleTabs = bottomBar.tabs.filter { resolver.isVisible(it.visibleIf) }
    if (visibleTabs.isEmpty()) return

    val resolvedSelectedId = selectedTabId?.takeIf { id -> visibleTabs.any { it.id == id } }
        ?: visibleTabs.first().id

    NavigationBar(modifier = Modifier.fillMaxWidth()) {
        visibleTabs.forEach { tab ->
            NavigationBarItem(
                selected = tab.id == resolvedSelectedId,
                onClick = { onTabSelected(tab) },
                icon = {
                    val content: @Composable () -> Unit = {
                        val customIcon = tab.icon
                        val iconUrl = tab.iconUrl
                        when {
                            customIcon != null -> RenderNode(
                                node = customIcon,
                                onAction = { onTabSelected(tab) },
                                resolver = resolver,
                            )
                            iconUrl?.isNotBlank() == true -> Text(iconUrl)
                            else -> Text(tab.title.take(2))
                        }
                    }
                    tab.badge?.let { badgeText ->
                        BadgedBox(badge = { Badge { Text(badgeText) } }) { content() }
                    } ?: content()
                },
                label = {
                    val customLabel = tab.label
                    when {
                        customLabel != null -> RenderNode(
                            node = customLabel,
                            onAction = { onTabSelected(tab) },
                            resolver = resolver,
                        )
                        else -> Text(text = tab.title)
                    }
                },
            )
        }
    }
}

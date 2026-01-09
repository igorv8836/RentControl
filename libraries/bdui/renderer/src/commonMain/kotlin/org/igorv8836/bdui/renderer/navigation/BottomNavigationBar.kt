package org.igorv8836.bdui.renderer.navigation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.igorv8836.bdui.contract.BottomBar
import org.igorv8836.bdui.contract.BottomTab
import org.igorv8836.bdui.renderer.binding.BindingResolver
import org.igorv8836.bdui.renderer.node.RenderNode
import androidx.compose.foundation.isSystemInDarkTheme
import org.igorv8836.bdui.renderer.node.parseColor

@Composable
internal fun BottomNavigationBar(
    bottomBar: BottomBar,
    resolver: BindingResolver,
    selectedTabId: String?,
    onTabSelected: (BottomTab) -> Unit,
) {
    val isDark = isSystemInDarkTheme()
    val visibleTabs = bottomBar.tabs.filter { resolver.isVisible(it.visibleIf) }
    if (visibleTabs.isEmpty()) return

    val resolvedSelectedId = selectedTabId?.takeIf { id -> visibleTabs.any { it.id == id } }
        ?: visibleTabs.first().id

    val containerColor = parseColor(bottomBar.containerColor, isDark)
    val selectedIconColor = parseColor(bottomBar.selectedIconColor, isDark)
    val unselectedIconColor = parseColor(bottomBar.unselectedIconColor, isDark)
    val selectedLabelColor = parseColor(bottomBar.selectedLabelColor, isDark)
    val unselectedLabelColor = parseColor(bottomBar.unselectedLabelColor, isDark)

    val itemColors = NavigationBarItemDefaults.colors(
        selectedIconColor = selectedIconColor ?: MaterialTheme.colorScheme.primary,
        unselectedIconColor = unselectedIconColor ?: MaterialTheme.colorScheme.onSurfaceVariant,
        selectedTextColor = selectedLabelColor ?: MaterialTheme.colorScheme.primary,
        unselectedTextColor = unselectedLabelColor ?: MaterialTheme.colorScheme.onSurfaceVariant,
    )

    NavigationBar(
        modifier = Modifier.fillMaxWidth(),
        containerColor = containerColor ?: MaterialTheme.colorScheme.surface,
    ) {
        visibleTabs.forEach { tab ->
            val badgeContainer = parseColor(tab.badgeBackgroundColor, isDark)
            val badgeContent = parseColor(tab.badgeTextColor, isDark)
            NavigationBarItem(
                selected = tab.id == resolvedSelectedId,
                onClick = { onTabSelected(tab) },
                colors = itemColors,
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
                        BadgedBox(
                            badge = {
                                val resolvedContainer = badgeContainer ?: MaterialTheme.colorScheme.error
                                val resolvedContent = badgeContent ?: MaterialTheme.colorScheme.onError
                                Badge(
                                    containerColor = resolvedContainer,
                                    contentColor = resolvedContent,
                                ) {
                                    Text(badgeText, color = Color.Unspecified)
                                }
                            },
                        ) { content() }
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

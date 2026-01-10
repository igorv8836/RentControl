package org.igorv8836.bdui.contract

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable

@Serializable
data class RemoteScreen(
    val id: String,
    val version: Int,
    val layout: Layout,
    val actions: List<@Polymorphic Action> = emptyList(),
    val triggers: List<Trigger> = emptyList(),
    val theme: Theme? = null,
    val settings: ScreenSettings = ScreenSettings(),
    val lifecycle: ScreenLifecycle = ScreenLifecycle(),
    val context: ExecutionContext = ExecutionContext(),
)

@Serializable
data class Layout(
    val root: ComponentNode? = null,
    val sections: List<Section> = emptyList(),
    val scaffold: Scaffold? = null,
)

@Serializable
data class Scaffold(
    val top: ComponentNode? = null,
    val bottom: ComponentNode? = null,
    val bottomBar: BottomBar? = null,
)

@Serializable
data class BottomBar(
    val tabs: List<BottomTab>,
    val selectedTabId: String? = null,
    val containerColor: Color? = null,
    val selectedIconColor: Color? = null,
    val unselectedIconColor: Color? = null,
    val selectedLabelColor: Color? = null,
    val unselectedLabelColor: Color? = null,
)

@Serializable
data class BottomTab(
    val id: String,
    val title: String,
    val actionId: String,
    val iconUrl: String? = null,
    val badge: String? = null,
    val badgeTextColor: Color? = null,
    val badgeBackgroundColor: Color? = null,
    val label: ComponentNode? = null,
    val icon: ComponentNode? = null,
    val visibleIf: Condition? = null,
)

@Serializable
data class Theme(
    val typography: Map<String, String> = emptyMap(),
    val colors: Map<String, String> = emptyMap(),
    val spacing: Map<String, Float> = emptyMap(),
)

@Serializable
data class Analytics(
    val event: String,
    val params: Map<String, String> = emptyMap(),
)

@Serializable
data class ValidationRule(
    val fieldId: String,
    val required: Boolean = false,
    val regex: String? = null,
    val message: String? = null,
)

@Serializable
data class Semantics(
    val label: String? = null,
    val hint: String? = null,
    val role: String? = null,
)

@Serializable
data class ExecutionContext(
    val screenId: String? = null,
    val parameters: Map<String, String> = emptyMap(),
    val metadata: Map<String, String> = emptyMap(),
)

@Serializable
data class Section(
    val id: String,
    val content: ComponentNode,
    val sticky: Sticky? = null,
    val scroll: SectionScroll = SectionScroll(),
    val visibleIf: Condition? = null,
)

@Serializable
data class SectionScroll(
    val enabled: Boolean = false,
    val orientation: ScrollOrientation = ScrollOrientation.Vertical,
    val reverse: Boolean = false,
    val userScrollEnabled: Boolean = true,
    val overscroll: Boolean = true,
    val contentPadding: Float? = null,
)

@Serializable
data class Sticky(
    val edge: StickyEdge,
    val mode: StickyMode = StickyMode.Always,
) {
    companion object {
        fun top(mode: StickyMode = StickyMode.Always): Sticky = Sticky(edge = StickyEdge.Top, mode = mode)

        fun bottom(mode: StickyMode = StickyMode.Always): Sticky = Sticky(edge = StickyEdge.Bottom, mode = mode)
    }
}

@Serializable
enum class StickyEdge {
    Top,
    Bottom,
}

@Serializable
enum class StickyMode {
    Always,
    OnScrollTowardsEdge,
}

@Serializable
enum class ScrollOrientation {
    Vertical, Horizontal
}

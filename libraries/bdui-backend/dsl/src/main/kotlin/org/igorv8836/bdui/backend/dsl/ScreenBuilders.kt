package org.igorv8836.bdui.backend.dsl

import org.igorv8836.bdui.backend.mapper.BackendScreenDraft
import org.igorv8836.bdui.backend.mapper.SectionBlueprint
import org.igorv8836.bdui.backend.mapper.SectionKey
import org.igorv8836.bdui.backend.mapper.SimpleSectionKey
import org.igorv8836.bdui.contract.Action
import org.igorv8836.bdui.contract.ComponentNode
import org.igorv8836.bdui.contract.ExecutionContext
import org.igorv8836.bdui.contract.Layout
import org.igorv8836.bdui.contract.PaginationSettings
import org.igorv8836.bdui.contract.PullToRefresh
import org.igorv8836.bdui.contract.RemoteScreen
import org.igorv8836.bdui.contract.Scaffold
import org.igorv8836.bdui.contract.ScreenLifecycle
import org.igorv8836.bdui.contract.ScreenSettings
import org.igorv8836.bdui.contract.Section
import org.igorv8836.bdui.contract.SectionScroll
import org.igorv8836.bdui.contract.SectionSticky
import org.igorv8836.bdui.contract.Theme
import org.igorv8836.bdui.contract.Trigger
import org.igorv8836.bdui.contract.UiEvent

/**
 * Entry point for constructing a Screen via DSL.
 */
fun screen(
    id: String,
    version: Int = 1,
    block: ScreenBuilder.() -> Unit,
): RemoteScreen {
    val builder = ScreenBuilder(id = id, version = version)
    builder.block()
    return builder.build()
}

/**
 * Screen builder that produces a blueprint (no rendering inside mapper).
 */
fun screenDraft(
    id: String,
    version: Int = 1,
    block: ScreenDraftBuilder.() -> Unit,
): BackendScreenDraft {
    val builder = ScreenDraftBuilder(id = id, version = version)
    builder.block()
    return builder.build()
}

class ScreenBuilder(
    private val id: String,
    private val version: Int,
) {
    private var root: ComponentNode? = null
    private val sections: MutableList<SectionDraft> = mutableListOf()
    private var scaffold: Scaffold? = null
    private val actions: MutableList<Action> = mutableListOf()
    private val triggers: MutableList<Trigger> = mutableListOf()
    private var theme: Theme? = null
    private var settings: ScreenSettings = ScreenSettings()
    private var lifecycle: ScreenLifecycle = ScreenLifecycle()
    private var context: ExecutionContext = ExecutionContext()

    fun layout(root: ComponentNode) {
        this.root = root
    }

    fun section(
        key: SectionKey,
        sticky: SectionSticky = SectionSticky.None,
        scroll: SectionScroll = SectionScroll(),
        visibleIf: org.igorv8836.bdui.contract.Condition? = null,
        renderer: SectionRenderer,
    ) {
        sections += SectionDraft(
            id = key.id,
            sticky = sticky,
            scroll = scroll,
            visibleIf = visibleIf,
            renderer = renderer,
            key = key,
        )
    }

    fun section(section: Section) {
        sections += SectionDraft(
            id = section.id,
            sticky = section.sticky,
            scroll = section.scroll,
            visibleIf = section.visibleIf,
            renderer = sectionRenderer { section.content },
            key = SimpleSectionKey(section.id),
        )
    }

    fun sections(block: SectionsBuilder.() -> Unit) {
        val builder = SectionsBuilder().apply(block)
        sections += builder.build()
    }

    fun scaffold(top: ComponentNode? = null, bottom: ComponentNode? = null, bottomBar: org.igorv8836.bdui.contract.BottomBar? = null) {
        this.scaffold = Scaffold(top = top, bottom = bottom, bottomBar = bottomBar)
    }

    fun settings(
        scrollable: Boolean = false,
        pullToRefresh: PullToRefresh = PullToRefresh(enabled = false),
        pagination: PaginationSettings = PaginationSettings(enabled = false),
    ) {
        this.settings = ScreenSettings(
            scrollable = scrollable,
            pullToRefresh = pullToRefresh,
            pagination = pagination,
        )
    }

    fun lifecycle(block: LifecycleBuilder.() -> Unit) {
        val builder = LifecycleBuilder()
        builder.block()
        lifecycle = builder.build()
    }

    fun context(value: ExecutionContext) {
        context = value
    }

    fun theme(value: Theme) {
        theme = value
    }

    fun action(action: Action) {
        actions += action
    }

    fun actions(vararg actions: Action) {
        this.actions += actions
    }

    fun trigger(trigger: Trigger) {
        triggers += trigger
    }

    fun triggers(vararg triggers: Trigger) {
        this.triggers += triggers
    }

    fun build(): RemoteScreen {
        val renderedSections = sections.map {
            Section(
                id = it.id,
                content = it.renderer.render(),
                sticky = it.sticky,
                scroll = it.scroll,
                visibleIf = it.visibleIf,
            )
        }

        val actualRoot = root ?: renderedSections.firstOrNull()?.content
            ?: error("Screen layout requires root or at least one section")
        return RemoteScreen(
            id = id,
            version = version,
            layout = Layout(root = actualRoot, sections = renderedSections, scaffold = scaffold),
            actions = actions.toList(),
            triggers = triggers.toList(),
            theme = theme,
            settings = settings,
            lifecycle = lifecycle,
            context = context,
        )
    }
}

class ScreenDraftBuilder(
    private val id: String,
    private val version: Int,
) {
    private val sections: MutableList<SectionBlueprint> = mutableListOf()
    private var scaffold: Scaffold? = null
    private val actions: MutableList<Action> = mutableListOf()
    private val triggers: MutableList<Trigger> = mutableListOf()
    private var theme: Theme? = null
    private var settings: ScreenSettings = ScreenSettings()
    private var lifecycle: ScreenLifecycle = ScreenLifecycle()
    private var context: ExecutionContext = ExecutionContext()

    fun section(
        key: SectionKey,
        sticky: SectionSticky = SectionSticky.None,
        scroll: SectionScroll = SectionScroll(),
        visibleIf: org.igorv8836.bdui.contract.Condition? = null,
    ) {
        sections += SectionBlueprint(
            key = key,
            sticky = sticky,
            scroll = scroll,
            visibleIf = visibleIf,
        )
    }

    fun sections(block: SectionsDraftBuilder.() -> Unit) {
        val builder = SectionsDraftBuilder().apply(block)
        sections += builder.build()
    }

    fun scaffold(top: ComponentNode? = null, bottom: ComponentNode? = null, bottomBar: org.igorv8836.bdui.contract.BottomBar? = null) {
        this.scaffold = Scaffold(top = top, bottom = bottom, bottomBar = bottomBar)
    }

    fun settings(
        scrollable: Boolean = false,
        pullToRefresh: PullToRefresh = PullToRefresh(enabled = false),
        pagination: PaginationSettings = PaginationSettings(enabled = false),
    ) {
        this.settings = ScreenSettings(
            scrollable = scrollable,
            pullToRefresh = pullToRefresh,
            pagination = pagination,
        )
    }

    fun lifecycle(block: LifecycleBuilder.() -> Unit) {
        val builder = LifecycleBuilder()
        builder.block()
        lifecycle = builder.build()
    }

    fun context(value: ExecutionContext) {
        context = value
    }

    fun theme(value: Theme) {
        theme = value
    }

    fun action(action: Action) {
        actions += action
    }

    fun actions(vararg actions: Action) {
        this.actions += actions
    }

    fun trigger(trigger: Trigger) {
        triggers += trigger
    }

    fun triggers(vararg triggers: Trigger) {
        this.triggers += triggers
    }

    fun build(): BackendScreenDraft {
        require(sections.isNotEmpty()) { "screenSpec requires at least one section" }
        return BackendScreenDraft(
            id = id,
            version = version,
            sections = sections.toList(),
            scaffold = scaffold,
            actions = actions.toList(),
            triggers = triggers.toList(),
            theme = theme,
            settings = settings,
            lifecycle = lifecycle,
            context = context,
        )
    }
}

class LifecycleBuilder {
    private val onOpen: MutableList<UiEvent> = mutableListOf()
    private val onAppear: MutableList<UiEvent> = mutableListOf()
    private val onFullyVisible: MutableList<UiEvent> = mutableListOf()

    fun onOpen(vararg events: UiEvent) {
        onOpen += events
    }

    fun onAppear(vararg events: UiEvent) {
        onAppear += events
    }

    fun onFullyVisible(vararg events: UiEvent) {
        onFullyVisible += events
    }

    fun build(): ScreenLifecycle = ScreenLifecycle(
        onOpen = onOpen.toList(),
        onAppear = onAppear.toList(),
        onFullyVisible = onFullyVisible.toList(),
    )
}

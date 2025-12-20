package org.igorv8836.bdui.backend.dsl

import org.igorv8836.bdui.contract.Action
import org.igorv8836.bdui.contract.Layout
import org.igorv8836.bdui.contract.Screen
import org.igorv8836.bdui.contract.ScreenLifecycle
import org.igorv8836.bdui.contract.ScreenSettings
import org.igorv8836.bdui.contract.Trigger
import org.igorv8836.bdui.contract.UiEvent
import org.igorv8836.bdui.contract.ExecutionContext
import org.igorv8836.bdui.contract.PullToRefresh
import org.igorv8836.bdui.contract.PaginationSettings
import org.igorv8836.bdui.contract.Scaffold
import org.igorv8836.bdui.contract.ComponentNode

/**
 * Entry point for constructing a Screen via DSL.
 */
fun screen(
    id: String,
    version: Int = 1,
    block: ScreenBuilder.() -> Unit,
): Screen {
    val builder = ScreenBuilder(id = id, version = version)
    builder.block()
    return builder.build()
}

class ScreenBuilder(
    private val id: String,
    private val version: Int,
) {
    private var root: ComponentNode? = null
    private var scaffold: Scaffold? = null
    private val actions: MutableList<Action> = mutableListOf()
    private val triggers: MutableList<Trigger> = mutableListOf()
    private var theme: org.igorv8836.bdui.contract.Theme? = null
    private var settings: ScreenSettings = ScreenSettings()
    private var lifecycle: ScreenLifecycle = ScreenLifecycle()
    private var context: ExecutionContext = ExecutionContext()

    fun layout(root: ComponentNode) {
        this.root = root
    }

    fun scaffold(top: ComponentNode? = null, bottom: ComponentNode? = null) {
        this.scaffold = Scaffold(top = top, bottom = bottom)
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

    fun theme(value: org.igorv8836.bdui.contract.Theme) {
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

    fun build(): Screen {
        val actualRoot = requireNotNull(root) { "Screen root layout is required" }
        return Screen(
            id = id,
            version = version,
            layout = Layout(root = actualRoot, scaffold = scaffold),
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

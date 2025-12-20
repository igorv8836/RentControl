package org.igorv8836.bdui.demo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import org.igorv8836.bdui.actions.ActionExecutor
import org.igorv8836.bdui.actions.ActionHandler
import org.igorv8836.bdui.actions.ActionRegistry
import org.igorv8836.bdui.actions.Navigator
import org.igorv8836.bdui.actions.Router
import org.igorv8836.bdui.actions.VariableAdapter
import org.igorv8836.bdui.contract.Binding
import org.igorv8836.bdui.contract.ButtonElement
import org.igorv8836.bdui.contract.ButtonKind
import org.igorv8836.bdui.contract.Condition
import org.igorv8836.bdui.contract.Container
import org.igorv8836.bdui.contract.ContainerDirection
import org.igorv8836.bdui.contract.DividerElement
import org.igorv8836.bdui.contract.ForwardAction
import org.igorv8836.bdui.contract.Layout
import org.igorv8836.bdui.contract.LazyListElement
import org.igorv8836.bdui.contract.ListItemElement
import org.igorv8836.bdui.contract.MissingVariableBehavior
import org.igorv8836.bdui.contract.Overlay
import org.igorv8836.bdui.contract.OverlayAction
import org.igorv8836.bdui.contract.OverlayKind
import org.igorv8836.bdui.contract.PaginationSettings
import org.igorv8836.bdui.contract.Popup
import org.igorv8836.bdui.contract.PopupAction
import org.igorv8836.bdui.contract.Route
import org.igorv8836.bdui.contract.RoutePresentation
import org.igorv8836.bdui.contract.Scaffold
import org.igorv8836.bdui.contract.Screen
import org.igorv8836.bdui.contract.ScreenLifecycle
import org.igorv8836.bdui.contract.ScreenSettings
import org.igorv8836.bdui.contract.SetVariableAction
import org.igorv8836.bdui.contract.TextElement
import org.igorv8836.bdui.contract.Trigger
import org.igorv8836.bdui.contract.TriggerSource
import org.igorv8836.bdui.contract.UiEvent
import org.igorv8836.bdui.contract.VariableScope
import org.igorv8836.bdui.contract.VariableValue
import org.igorv8836.bdui.contract.IncrementVariableAction
import org.igorv8836.bdui.renderer.ScreenHost
import org.igorv8836.bdui.runtime.ScreenState
import org.igorv8836.bdui.runtime.ScreenStatus
import org.igorv8836.bdui.runtime.VariableStore

@Composable
fun DemoScreen(
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val variables = remember { VariableStore(scope = scope) }

    LaunchedEffect(Unit) {
        variables.set(
            key = "user_name",
            value = VariableValue.StringValue("Guest"),
            scope = VariableScope.Global,
        )
        variables.set(
            key = "visits",
            value = VariableValue.NumberValue(0.0),
            scope = VariableScope.Global,
        )
    }

    val startAction = ForwardAction(id = "start-flow", path = "/flow/start", presentation = RoutePresentation.Push)
    val browseAction = ForwardAction(id = "browse", path = "/catalog")
    val detailActions = List(6) { index ->
        ForwardAction(id = "details-${index + 1}", path = "/details/${index + 1}")
    }
    val overlayAction = OverlayAction(
        id = "toast",
        overlay = Overlay(kind = OverlayKind.Toast, payload = mapOf("message" to "Data refreshed")),
    )
    val popupAction = PopupAction(
        id = "popup-rate",
        popup = Popup(payload = mapOf("title" to "Rate your experience")),
    )
    val setFilter = SetVariableAction(
        id = "set-filter",
        key = "active_filter",
        value = VariableValue.StringValue("wifi"),
        scope = VariableScope.Global,
    )
    val setPremium = SetVariableAction(
        id = "set-premium",
        key = "is_premium",
        value = VariableValue.BoolValue(true),
        scope = VariableScope.Global,
    )
    val incVisits = IncrementVariableAction(
        id = "inc-visits",
        key = "visits",
        scope = VariableScope.Global,
    )
    val setUser = SetVariableAction(
        id = "set-user",
        key = "user_name",
        value = VariableValue.StringValue("Alex"),
        scope = VariableScope.Global,
    )

    val triggers = listOf(
        Trigger(
            id = "show-popup-on-premium",
            source = TriggerSource.VariableChanged(key = "is_premium", scope = VariableScope.Global),
            condition = Condition(
                binding = Binding("is_premium", scope = VariableScope.Global),
                equals = VariableValue.BoolValue(true),
            ),
            actions = listOf(popupAction),
            throttleMs = 2_000,
        ),
    )

    val screen = Screen(
        id = "demo-full",
        version = 2,
        layout = Layout(
            scaffold = Scaffold(
                top = Container(
                    id = "top-bar",
                    direction = ContainerDirection.Row,
                    spacing = 8f,
                    children = listOf(
                        TextElement(
                            id = "welcome",
                            textKey = "welcome",
                            template = "Hello, {{user_name}} ({{visits}} visits)",
                        ),
                        ButtonElement(
                            id = "set-user-btn",
                            titleKey = "set_user",
                            actionId = setUser.id,
                            kind = ButtonKind.Secondary,
                        ),
                    ),
                ),
                bottom = Container(
                    id = "bottom-bar",
                    direction = ContainerDirection.Row,
                    spacing = 12f,
                    children = listOf(
                        ButtonElement(
                            id = "apply-filter",
                            titleKey = "apply_filter",
                            actionId = setFilter.id,
                            kind = ButtonKind.Primary,
                        ),
                        ButtonElement(
                            id = "mark-premium",
                            titleKey = "mark_premium",
                            actionId = setPremium.id,
                            kind = ButtonKind.Secondary,
                        ),
                    ),
                ),
            ),
            root = Container(
                id = "root",
                direction = ContainerDirection.Column,
                spacing = 16f,
                children = listOf(
                    TextElement(
                        id = "hero-title",
                        textKey = "hero_title",
                        template = "Backend-driven Playground",
                    ),
                    TextElement(
                        id = "hero-subtitle",
                        textKey = "hero_subtitle",
                        binding = Binding(
                            key = "active_filter",
                            scope = VariableScope.Global,
                            default = VariableValue.StringValue("No filters"),
                            missingBehavior = MissingVariableBehavior.Default,
                        ),
                        template = "Active filter: {{active_filter}}",
                    ),
                    Container(
                        id = "cta-row",
                        direction = ContainerDirection.Row,
                        spacing = 12f,
                        children = listOf(
                            ButtonElement(
                                id = "cta-start",
                                titleKey = "cta_start",
                                actionId = startAction.id,
                                kind = ButtonKind.Primary,
                            ),
                            ButtonElement(
                                id = "cta-browse",
                                titleKey = "cta_browse",
                                actionId = browseAction.id,
                                kind = ButtonKind.Secondary,
                            ),
                        ),
                    ),
                    DividerElement(id = "divider-top"),
                    LazyListElement(
                        id = "offers",
                        items = detailActions.map { action ->
                            ListItemElement(
                                id = "item-${action.id}",
                                titleKey = "card_title_${action.id}",
                                subtitleKey = "card_desc_${action.id}",
                                actionId = action.id,
                                titleBinding = Binding(
                                    key = "card_override_${action.id}",
                                    missingBehavior = MissingVariableBehavior.Empty,
                                ),
                            )
                        },
                        placeholderCount = 2,
                    ),
                    DividerElement(id = "divider-bottom"),
                    Container(
                        id = "switches",
                        direction = ContainerDirection.Column,
                        spacing = 8f,
                        children = listOf(
                            ButtonElement(
                                id = "refresh-btn",
                                titleKey = "refresh",
                                actionId = overlayAction.id,
                                kind = ButtonKind.Primary,
                            ),
                            ButtonElement(
                                id = "increment-visit",
                                titleKey = "inc_visit",
                                actionId = incVisits.id,
                                kind = ButtonKind.Secondary,
                            ),
                        ),
                    ),
                    TextElement(
                        id = "premium-badge",
                        textKey = "premium_badge",
                        visibleIf = Condition(
                            binding = Binding("is_premium", scope = VariableScope.Global),
                            equals = VariableValue.BoolValue(true),
                        ),
                    ),
                ),
            ),
        ),
        actions = listOf(
            startAction,
            browseAction,
            overlayAction,
            popupAction,
            setFilter,
            setPremium,
            setUser,
            incVisits,
        ) + detailActions,
        settings = ScreenSettings(
            scrollable = true,
            pullToRefresh = org.igorv8836.bdui.contract.PullToRefresh(enabled = true),
            pagination = PaginationSettings(enabled = true, pageParam = "page", prefetchDistance = 1),
        ),
        lifecycle = ScreenLifecycle(
            onOpen = listOf(
                UiEvent(
                    id = "open-analytics",
                    actions = listOf(
                        PopupAction(
                            id = "lifecycle-popup",
                            popup = Popup(payload = mapOf("title" to "Lifecycle onOpen fired")),
                        ),
                    ),
                ),
            ),
        ),
        triggers = triggers,
    )

    val state = ScreenState(
        screen = screen,
        status = ScreenStatus.Ready,
    )

    val router = object : Router {
        override fun navigate(route: Route) {
            println("Navigate to ${route.destination} (${route.presentation})")
        }
    }

    val navigator = object : Navigator {
        override fun openRoute(route: Route, parameters: Map<String, String>) {
            println("Open route ${route.destination} with $parameters")
        }

        override fun forward(path: String?, screen: Screen?, parameters: Map<String, String>) {
            println("Forward to $path or embedded screen ${screen?.id} with $parameters")
        }

        override fun showPopup(popup: Popup, parameters: Map<String, String>) {
            println("Show popup ${popup.payload} $parameters")
        }

        override fun showOverlay(overlay: Overlay, parameters: Map<String, String>) {
            println("Show overlay ${overlay.kind} $parameters")
        }
    }

    val variableAdapter = object : VariableAdapter {
        override fun peek(key: String, scope: VariableScope, screenId: String?): VariableValue? =
            variables.peek(key, scope, screenId)

        override suspend fun set(
            key: String,
            value: VariableValue,
            scope: VariableScope,
            screenId: String?,
            policy: org.igorv8836.bdui.contract.StoragePolicy,
            ttlMillis: Long?,
        ) {
            variables.set(key, value, scope, screenId, policy, ttlMillis)
        }

        override suspend fun increment(
            key: String,
            delta: Double,
            scope: VariableScope,
            screenId: String?,
            policy: org.igorv8836.bdui.contract.StoragePolicy,
        ) {
            variables.increment(key, delta, scope, screenId, policy)
        }

        override suspend fun remove(key: String, scope: VariableScope, screenId: String?) {
            variables.remove(key, scope, screenId)
        }
    }

    val executor = ActionExecutor(
        navigator = navigator,
        analytics = { event, params -> println("analytics: $event $params") },
        variables = variableAdapter,
    )

    val registry = ActionRegistry(
        handlers = buildMap {
            detailActions.forEach { action ->
                put(action.id, ActionHandler { _, context ->
                    val destination = action.path ?: "details/${action.id}"
                    context.router.navigate(route = Route(destination = destination))
                })
            }
        },
        fallback = ActionHandler { action, context ->
            executor.execute(action, context)
        },
    )

    val onRefresh: () -> Unit = {
        scope.launch {
            variables.set(
                key = "visits",
                value = VariableValue.NumberValue(1.0),
                scope = VariableScope.Global,
            )
        }
    }

    ScreenHost(
        state = state,
        router = router,
        actionRegistry = registry,
        resolve = { key ->
            when (key) {
                "welcome" -> "Welcome!"
                "set_user" -> "Set user"
                "apply_filter" -> "Apply Wi‑Fi filter"
                "mark_premium" -> "Mark premium"
                "hero_title" -> "Backend-driven UI Playground"
                "hero_subtitle" -> "Dynamic layouts, actions, variables"
                "cta_start" -> "Start flow"
                "cta_browse" -> "Browse catalog"
                "card_title_details-1" -> "City apartment"
                "card_desc_details-1" -> "Downtown, park view"
                "card_title_details-2" -> "Loft by the river"
                "card_desc_details-2" -> "Fast Wi‑Fi, parking"
                "card_title_details-3" -> "Lake house"
                "card_desc_details-3" -> "Fireplace, 3 beds"
                "card_title_details-4" -> "Studio"
                "card_desc_details-4" -> "Compact and bright"
                "card_title_details-5" -> "Country cottage"
                "card_desc_details-5" -> "Silence and nature"
                "card_title_details-6" -> "Penthouse"
                "card_desc_details-6" -> "Panoramic view"
                "refresh" -> "Pull to refresh"
                "inc_visit" -> "Add visit"
                "premium_badge" -> "Premium user"
                else -> key
            }
        },
        variableStore = variables,
        screenId = state.screen?.id,
        analytics = { event, params -> println("analytics: $event $params") },
        onRefresh = onRefresh,
        onLoadNextPage = { println("load next page request") },
        onAppear = { println("onAppear") },
        onFullyVisible = { println("onFullyVisible") },
        onDisappear = { println("onDisappear") },
        modifier = modifier,
    )
}

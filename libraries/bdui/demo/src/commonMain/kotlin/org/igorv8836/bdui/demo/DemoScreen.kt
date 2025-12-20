package org.igorv8836.bdui.demo

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.igorv8836.bdui.actions.ActionContext
import org.igorv8836.bdui.actions.ActionHandler
import org.igorv8836.bdui.actions.ActionRegistry
import org.igorv8836.bdui.actions.Router
import org.igorv8836.bdui.contract.Action
import org.igorv8836.bdui.contract.ActionType
import org.igorv8836.bdui.contract.ButtonElement
import org.igorv8836.bdui.contract.ButtonKind
import org.igorv8836.bdui.contract.Container
import org.igorv8836.bdui.contract.ContainerDirection
import org.igorv8836.bdui.contract.ImageElement
import org.igorv8836.bdui.contract.Layout
import org.igorv8836.bdui.contract.LazyListElement
import org.igorv8836.bdui.contract.Route
import org.igorv8836.bdui.contract.Screen
import org.igorv8836.bdui.contract.TextElement
import org.igorv8836.bdui.renderer.ScreenHost
import org.igorv8836.bdui.runtime.ScreenState
import org.igorv8836.bdui.runtime.ScreenStatus

@Composable
fun DemoScreen(
    modifier: Modifier = Modifier,
) {
    // Actions registry
    val startAction = Action(id = "start-flow", type = ActionType.Navigate)
    val browseAction = Action(id = "browse", type = ActionType.Navigate)
    val detailActions = listOf(
        Action(id = "details-1", type = ActionType.Navigate),
        Action(id = "details-2", type = ActionType.Navigate),
        Action(id = "details-3", type = ActionType.Navigate),
    )
    val analyticsAction = Action(id = "rate", type = ActionType.Analytics)
    val supportAction = Action(id = "support", type = ActionType.Custom)

    // Sample screen tree with nested layout and list
    val screen = Screen(
        id = "demo-complex",
        version = 1,
        layout = Layout(
            root = Container(
                id = "root",
                direction = ContainerDirection.Column,
                spacing = 16f,
                children = listOf(
                    TextElement(id = "hero-title", textKey = "hero_title"),
                    TextElement(id = "hero-subtitle", textKey = "hero_subtitle"),
                    ImageElement(
                        id = "hero-image",
                        url = "https://example.com/hero.png",
                        description = "Hero image placeholder",
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
                    TextElement(id = "list-title", textKey = "list_title"),
                    LazyListElement(
                        id = "offers",
                        items = detailActions.map { action ->
                            Container(
                                id = "card-${action.id}",
                                direction = ContainerDirection.Column,
                                spacing = 8f,
                                children = listOf(
                                    TextElement(
                                        id = "card-title-${action.id}",
                                        textKey = "card_title_${action.id}",
                                    ),
                                    TextElement(
                                        id = "card-desc-${action.id}",
                                        textKey = "card_desc_${action.id}",
                                    ),
                                    ButtonElement(
                                        id = "card-cta-${action.id}",
                                        titleKey = "card_cta_${action.id}",
                                        actionId = action.id,
                                        kind = ButtonKind.Primary,
                                    ),
                                ),
                            )
                        },
                        placeholderCount = 0,
                    ),
                    Container(
                        id = "footer",
                        direction = ContainerDirection.Column,
                        spacing = 8f,
                        children = listOf(
                            ButtonElement(
                                id = "footer-analytics",
                                titleKey = "footer_rate",
                                actionId = analyticsAction.id,
                                kind = ButtonKind.Secondary,
                            ),
                            ButtonElement(
                                id = "footer-support",
                                titleKey = "footer_support",
                                actionId = supportAction.id,
                                kind = ButtonKind.Ghost,
                            ),
                        ),
                    ),
                ),
            ),
        ),
        actions = listOf(
            startAction,
            browseAction,
            analyticsAction,
            supportAction,
        ) + detailActions,
    )

    val state = ScreenState(
        screen = screen,
        status = ScreenStatus.Ready,
    )

    val router = object : Router {
        override fun navigate(route: Route) {
            println("Navigate to ${route.destination}")
        }
    }

    val registry = ActionRegistry(
        handlers = buildMap {
            put(startAction.id, ActionHandler { _, context ->
                context.router.navigate(route = Route(destination = "flow/start"))
                context.analytics("start_flow", emptyMap())
            })
            put(browseAction.id, ActionHandler { _, context ->
                context.router.navigate(route = Route(destination = "catalog"))
            })
            detailActions.forEach { action ->
                put(action.id, ActionHandler { _, context ->
                    context.router.navigate(route = Route(destination = "details/${action.id}"))
                })
            }
            put(analyticsAction.id, ActionHandler { _, context ->
                context.analytics("rate_clicked", mapOf("source" to "footer"))
            })
            put(supportAction.id, ActionHandler { _, context ->
                println("Open support chat")
                context.analytics("support_opened", emptyMap())
            })
        },
    )

    ScreenHost(
        state = state,
        router = router,
        actionRegistry = registry,
        resolve = { key ->
            when (key) {
                "hero_title" -> "Backend-driven UI Playground"
                "hero_subtitle" -> "Динамическое дерево компонентов, отданное с сервера"
                "cta_start" -> "Начать"
                "cta_browse" -> "Каталог"
                "list_title" -> "Подборки для вас"
                "card_title_details-1" -> "Городские апартаменты"
                "card_desc_details-1" -> "Центр, 2 спальни, вид на парк"
                "card_cta_details-1" -> "Смотреть"
                "card_title_details-2" -> "Лофт у набережной"
                "card_desc_details-2" -> "Студия, быстрый Wi‑Fi, парковка"
                "card_cta_details-2" -> "Подробнее"
                "card_title_details-3" -> "Дом у озера"
                "card_desc_details-3" -> "3 спальни, камин, тишина"
                "card_cta_details-3" -> "Выбрать"
                "footer_rate" -> "Оценить подборку"
                "footer_support" -> "Нужна помощь"
                else -> key
            }
        },
        modifier = modifier,
    )
}

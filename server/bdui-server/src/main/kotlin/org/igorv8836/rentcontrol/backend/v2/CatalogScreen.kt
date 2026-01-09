package org.igorv8836.rentcontrol.backend.v2

import org.igorv8836.bdui.backend.core.BackendResult
import org.igorv8836.bdui.backend.core.RenderContext
import org.igorv8836.bdui.backend.runtime.annotations.DraftBinding
import org.igorv8836.bdui.backend.runtime.engine.DraftMapper
import org.igorv8836.bdui.backend.runtime.engine.FetcherContext
import org.igorv8836.bdui.backend.runtime.engine.Parameters
import org.igorv8836.bdui.backend.runtime.engine.Renderer
import org.igorv8836.bdui.backend.runtime.engine.RenderingData
import org.igorv8836.bdui.backend.runtime.annotations.ScaffoldBinding
import org.igorv8836.bdui.backend.runtime.engine.ScaffoldDraft
import org.igorv8836.bdui.backend.runtime.annotations.ScreenBinding
import org.igorv8836.bdui.backend.runtime.engine.ScreenBuilder
import org.igorv8836.bdui.backend.runtime.engine.ScreenDraft
import org.igorv8836.bdui.backend.runtime.engine.SectionDraft
import org.igorv8836.bdui.backend.runtime.engine.SectionKey
import org.igorv8836.bdui.backend.dsl.container
import org.igorv8836.bdui.contract.ComponentNode
import org.igorv8836.bdui.contract.ContainerDirection
import org.igorv8836.bdui.contract.ForwardAction
import org.igorv8836.bdui.contract.PullToRefresh
import org.igorv8836.bdui.contract.ScreenSettings
import org.igorv8836.bdui.contract.Scaffold
import org.igorv8836.bdui.contract.SectionScroll
import org.igorv8836.rentcontrol.backend.model.OfferDto

object CatalogParams : Parameters

object CatalogContentKey : SectionKey { override val id: String = "catalog-content" }
object CatalogScaffoldKey : SectionKey { override val id: String = "catalog-scaffold" }

data class CatalogContentData(val offers: List<OfferDto>) : RenderingData
data class CatalogScaffoldData(
    val tabs: List<TabData>,
    val selectedTabId: String?,
) : RenderingData

data class TabData(
    val id: String,
    val title: String,
    val actionId: String,
)

@ScreenBinding(
    params = CatalogParams::class,
    builder = CatalogScreenBuilder::class,
    scanPackages = ["org.igorv8836.rentcontrol.backend.v2"],
)
class CatalogScreenBuilder : ScreenBuilder<CatalogParams> {
    override fun build(params: CatalogParams): ScreenDraft = ScreenDraft(
        sections = listOf(
            SectionDraft(key = CatalogContentKey, scroll = SectionScroll(enabled = true)),
        ),
        scaffold = ScaffoldDraft(key = CatalogScaffoldKey),
        settings = ScreenSettings(scrollable = true, pullToRefresh = PullToRefresh(enabled = true)),
        actions = emptyList(),
        triggers = emptyList(),
    )
}

@DraftBinding(
    key = CatalogContentKey::class,
    mapper = CatalogContentMapper::class,
    renderModel = CatalogContentData::class,
    renderer = CatalogContentRenderer::class,
)
class CatalogContentMapper : DraftMapper<SectionDraft, CatalogContentData> {
    override suspend fun map(draft: SectionDraft, params: Parameters, fetchers: FetcherContext): BackendResult<CatalogContentData> =
        fetchers.fetch(OffersFetcherV2()).map { offers -> CatalogContentData(offers) }
}

class CatalogContentRenderer : Renderer<CatalogContentData, ComponentNode> {
    override fun render(data: CatalogContentData, ctx: RenderContext): ComponentNode {
        val goHome = ForwardAction(id = "go-home", path = "home")
        val goCatalog = ForwardAction(id = "go-catalog", path = "catalog")
        return container(
            id = "catalog-root",
            direction = ContainerDirection.Column,
            spacing = 12f,
            ctx = ctx,
        ) {
            text(id = "catalog-title", text = "Catalog")
            text(
                id = "catalog-subtitle",
                text = "Pick any item to open its details. The list is served from the backend.",
            )
            button(
                id = "catalog-home",
                title = "Back to home",
                action = goHome,
            )
            list(id = "catalog-list", placeholderCount = 0, ctx = ctx) {
                data.offers.forEach { offer ->
                    val action = ForwardAction(id = "catalog-details-${offer.id}", path = "details/${offer.id}")
                    listItem(
                        id = "catalog-${offer.id}",
                        title = offer.title,
                        subtitle = offer.subtitle,
                        action = action,
                    )
                }
            }
        }
    }
}

@ScaffoldBinding(
    key = CatalogScaffoldKey::class,
    mapper = CatalogScaffoldMapper::class,
    renderModel = CatalogScaffoldData::class,
    renderer = CatalogScaffoldRenderer::class,
)
class CatalogScaffoldMapper : DraftMapper<ScaffoldDraft, CatalogScaffoldData> {
    override suspend fun map(draft: ScaffoldDraft, params: Parameters, fetchers: FetcherContext): BackendResult<CatalogScaffoldData> =
        BackendResult.success(
            CatalogScaffoldData(
                tabs = listOf(
                    TabData(id = "home-tab", title = "Home", actionId = "go-home"),
                    TabData(id = "catalog-tab", title = "Catalog", actionId = "go-catalog"),
                ),
                selectedTabId = "catalog-tab",
            ),
        )
}

class CatalogScaffoldRenderer : Renderer<CatalogScaffoldData, Scaffold> {
    override fun render(data: CatalogScaffoldData, ctx: RenderContext): Scaffold {
        val goHome = ForwardAction(id = "go-home", path = "home")
        val goCatalog = ForwardAction(id = "go-catalog", path = "catalog")
        val bar = org.igorv8836.bdui.contract.BottomBar(
            tabs = data.tabs.map { tab ->
                val action = when (tab.id) {
                    "home-tab" -> goHome
                    "catalog-tab" -> goCatalog
                    else -> ForwardAction(id = tab.actionId, path = tab.actionId)
                }
                ctx.register(action)
                org.igorv8836.bdui.contract.BottomTab(
                    id = tab.id,
                    title = tab.title,
                    actionId = action.id,
                )
            },
            selectedTabId = data.selectedTabId,
        )
        return Scaffold(
            top = null,
            bottom = null,
            bottomBar = bar,
        )
    }
}

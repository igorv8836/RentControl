package org.igorv8836.rentcontrol.backend.v2

import org.igorv8836.bdui.backend.core.BackendError
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

data class DetailsParams(val id: String) : Parameters

object DetailsContentKey : SectionKey { override val id: String = "details-content" }
object DetailsScaffoldKey : SectionKey { override val id: String = "details-scaffold" }

data class DetailsContentData(val offer: OfferDto) : RenderingData
data class DetailsScaffoldData(
    val tabs: List<TabData>,
    val selectedTabId: String?,
) : RenderingData

@ScreenBinding(
    params = DetailsParams::class,
    builder = DetailsScreenBuilder::class,
    scanPackages = ["org.igorv8836.rentcontrol.backend.v2"],
)
class DetailsScreenBuilder : ScreenBuilder<DetailsParams> {
    override fun build(params: DetailsParams): ScreenDraft = ScreenDraft(
        sections = listOf(
            SectionDraft(key = DetailsContentKey, scroll = SectionScroll(enabled = true)),
        ),
        scaffold = ScaffoldDraft(key = DetailsScaffoldKey),
        settings = ScreenSettings(scrollable = true, pullToRefresh = PullToRefresh(enabled = true)),
        actions = emptyList(),
        triggers = emptyList(),
    )
}

@DraftBinding(
    key = DetailsContentKey::class,
    mapper = DetailsContentMapper::class,
    renderModel = DetailsContentData::class,
    renderer = DetailsContentRenderer::class,
)
class DetailsContentMapper : DraftMapper<SectionDraft, DetailsContentData> {
    override suspend fun map(draft: SectionDraft, params: Parameters, fetchers: FetcherContext): BackendResult<DetailsContentData> {
        val detailsParams = params as? DetailsParams
            ?: return BackendResult.failure(BackendError.Mapping("Invalid params for details"))
        val offers = fetchers.fetch(OffersFetcherV2())
        return offers.flatMap { list ->
            val offer = list.find { it.id == detailsParams.id }
                ?: return BackendResult.failure(BackendError.Mapping("Offer not found: ${detailsParams.id}"))
            BackendResult.success(DetailsContentData(offer))
        }
    }
}

class DetailsContentRenderer : Renderer<DetailsContentData, ComponentNode> {
    override fun render(data: DetailsContentData, ctx: RenderContext): ComponentNode {
        val goHome = ForwardAction(id = "go-home", path = "home")
        val goCatalog = ForwardAction(id = "go-catalog", path = "catalog")
        return container(
            id = "details-root",
            direction = ContainerDirection.Column,
            spacing = 12f,
            ctx = ctx,
        ) {
            text(id = "details-title", text = data.offer.title)
            text(id = "details-subtitle", text = data.offer.subtitle)
            text(id = "details-description", text = data.offer.description)
            container(
                id = "details-actions",
                direction = ContainerDirection.Row,
                spacing = 12f,
                ctx = ctx,
            ) {
                button(
                    id = "details-home",
                    title = "Back to home",
                    action = goHome,
                )
                button(
                    id = "details-catalog",
                    title = "Open catalog",
                    action = goCatalog,
                )
            }
        }
    }
}

@ScaffoldBinding(
    key = DetailsScaffoldKey::class,
    mapper = DetailsScaffoldMapper::class,
    renderModel = DetailsScaffoldData::class,
    renderer = DetailsScaffoldRenderer::class,
)
class DetailsScaffoldMapper : DraftMapper<ScaffoldDraft, DetailsScaffoldData> {
    override suspend fun map(draft: ScaffoldDraft, params: Parameters, fetchers: FetcherContext): BackendResult<DetailsScaffoldData> =
        BackendResult.success(
            DetailsScaffoldData(
                tabs = listOf(
                    TabData(id = "home-tab", title = "Home", actionId = "go-home"),
                    TabData(id = "catalog-tab", title = "Catalog", actionId = "go-catalog"),
                ),
                selectedTabId = "catalog-tab",
            ),
        )
}

class DetailsScaffoldRenderer : Renderer<DetailsScaffoldData, Scaffold> {
    override fun render(data: DetailsScaffoldData, ctx: RenderContext): Scaffold {
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

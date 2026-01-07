package org.igorv8836.rentcontrol.backend.v2

import org.igorv8836.bdui.backend.core.BackendResult
import org.igorv8836.bdui.backend.runtime.engine.DraftMapper
import org.igorv8836.bdui.backend.runtime.annotations.DraftBinding
import org.igorv8836.bdui.backend.runtime.engine.FetcherContext
import org.igorv8836.bdui.backend.runtime.engine.Parameters
import org.igorv8836.bdui.backend.runtime.engine.SectionDraft
import org.igorv8836.bdui.backend.runtime.engine.ScaffoldDraft
import org.igorv8836.bdui.backend.runtime.annotations.ScaffoldBinding
import org.igorv8836.rentcontrol.backend.model.OfferDto

@DraftBinding(
    key = HomeHeaderKey::class,
    mapper = HomeHeaderMapper::class,
    renderModel = HomeHeaderData::class,
    renderer = HomeHeaderRenderer::class,
)
class HomeHeaderMapper : DraftMapper<SectionDraft, HomeHeaderData> {
    override suspend fun map(draft: SectionDraft, params: Parameters, fetchers: FetcherContext): BackendResult<HomeHeaderData> =
        BackendResult.success(HomeHeaderData(userName = "Guest"))
}

@DraftBinding(
    key = HomeOffersKey::class,
    mapper = HomeOffersMapper::class,
    renderModel = HomeOffersData::class,
    renderer = HomeOffersRenderer::class,
)
class HomeOffersMapper : DraftMapper<SectionDraft, HomeOffersData> {
    override suspend fun map(draft: SectionDraft, params: Parameters, fetchers: FetcherContext): BackendResult<HomeOffersData> {
        val offersResult = fetchers.fetch(OffersFetcherV2())
        return offersResult.map { offers -> HomeOffersData(offers) }
    }
}

@DraftBinding(
    key = HomeFooterKey::class,
    mapper = HomeFooterMapper::class,
    renderModel = HomeFooterData::class,
    renderer = HomeFooterRenderer::class,
)
class HomeFooterMapper : DraftMapper<SectionDraft, HomeFooterData> {
    override suspend fun map(draft: SectionDraft, params: Parameters, fetchers: FetcherContext): BackendResult<HomeFooterData> =
        BackendResult.success(
            HomeFooterData(
                refreshActionId = "set-user",
                incVisitsActionId = "inc-visits",
            ),
        )
}

@ScaffoldBinding(
    key = HomeScaffoldKey::class,
    mapper = HomeScaffoldMapper::class,
    renderModel = HomeScaffoldData::class,
    renderer = HomeScaffoldRenderer::class,
)
class HomeScaffoldMapper : DraftMapper<ScaffoldDraft, HomeScaffoldData> {
    override suspend fun map(draft: ScaffoldDraft, params: Parameters, fetchers: FetcherContext): BackendResult<HomeScaffoldData> =
        BackendResult.success(HomeScaffoldData(bottomBar = BottomBars.home()))
}

private object BottomBars {
    fun home() = org.igorv8836.bdui.contract.BottomBar(
        tabs = listOf(
            org.igorv8836.bdui.contract.BottomTab(id = "home-tab", title = "Home", actionId = "go-home"),
            org.igorv8836.bdui.contract.BottomTab(id = "catalog-tab", title = "Catalog", actionId = "go-catalog"),
        ),
        selectedTabId = "home-tab",
    )
}

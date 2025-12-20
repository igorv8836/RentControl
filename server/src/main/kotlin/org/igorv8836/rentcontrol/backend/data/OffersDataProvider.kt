package org.igorv8836.rentcontrol.backend.data

import org.igorv8836.bdui.backend.core.BackendResult
import org.igorv8836.bdui.backend.data.DataPolicy
import org.igorv8836.bdui.backend.data.DataProvider
import org.igorv8836.rentcontrol.backend.model.OfferDto

class OffersDataProvider : DataProvider<Unit, List<OfferDto>> {
    override suspend fun fetch(request: Unit, policy: DataPolicy): BackendResult<List<OfferDto>> {
        val mock = listOf(
            OfferDto(id = "1", title = "Loft with river view", subtitle = "Fast Wi‑Fi, parking"),
            OfferDto(id = "2", title = "Lake house", subtitle = "Fireplace, 3 beds"),
            OfferDto(id = "3", title = "City studio", subtitle = "Compact, bright, near metro"),
            OfferDto(id = "4", title = "Country cottage", subtitle = "Silence and nature"),
        )
        return BackendResult.success(mock)
    }
}

package org.igorv8836.rentcontrol.backend.data

import org.igorv8836.bdui.backend.core.BackendResult
import org.igorv8836.bdui.backend.data.DataPolicy
import org.igorv8836.bdui.backend.data.DataProvider
import org.igorv8836.rentcontrol.backend.model.OfferDto

class OffersDataProvider : DataProvider<Unit, List<OfferDto>> {
    override suspend fun fetch(request: Unit, policy: DataPolicy): BackendResult<List<OfferDto>> {
        val mock = listOf(
            OfferDto(
                id = "1",
                title = "Loft with river view",
                subtitle = "Fast Wi‑Fi, parking",
                description = "Open-space loft facing the river with floor-to-ceiling windows and secure parking.",
            ),
            OfferDto(
                id = "2",
                title = "Lake house",
                subtitle = "Fireplace, 3 beds",
                description = "Quiet lake house with wooden interiors, fireplace, and a terrace for morning coffee.",
            ),
            OfferDto(
                id = "3",
                title = "City studio",
                subtitle = "Compact, bright, near metro",
                description = "Compact studio in the heart of the city, 3 minutes from the metro, bright and cozy.",
            ),
            OfferDto(
                id = "4",
                title = "Country cottage",
                subtitle = "Silence and nature",
                description = "Spacious cottage surrounded by forest, perfect for weekend escapes with friends.",
            ),
        )
        return BackendResult.success(mock)
    }
}

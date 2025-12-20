package org.igorv8836.rentcontrol.backend.mapper

import kotlinx.coroutines.runBlocking
import org.igorv8836.bdui.backend.core.BackendError
import org.igorv8836.bdui.backend.core.BackendResult
import org.igorv8836.bdui.backend.core.ExecutionContext
import org.igorv8836.bdui.backend.data.DataPolicy
import org.igorv8836.bdui.backend.mapper.ScreenMapper
import org.igorv8836.rentcontrol.backend.data.OffersDataProvider
import org.igorv8836.rentcontrol.backend.screen.buildDetailsScreen

class DetailsScreenMapper(
    private val offersProvider: OffersDataProvider,
) : ScreenMapper<String> {
    override fun map(input: String, context: ExecutionContext): BackendResult<org.igorv8836.bdui.contract.RemoteScreen> =
        runBlocking {
            offersProvider.fetch(Unit, DataPolicy()).flatMap { offers ->
                val offer = offers.firstOrNull { it.id == input }
                    ?: return@flatMap BackendResult.failure(
                        BackendError.Mapping(message = "Offer with id=$input not found"),
                    )
                BackendResult.success(buildDetailsScreen(offer))
            }
        }
}

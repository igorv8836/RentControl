package org.igorv8836.rentcontrol.backend.mapper

import kotlinx.coroutines.runBlocking
import org.igorv8836.bdui.backend.core.BackendResult
import org.igorv8836.bdui.backend.data.DataPolicy
import org.igorv8836.bdui.backend.mapper.ScreenMapper
import org.igorv8836.bdui.contract.Screen
import org.igorv8836.rentcontrol.backend.data.OffersDataProvider
import org.igorv8836.rentcontrol.backend.screen.buildHomeScreen

class HomeScreenMapper(
    private val offersProvider: OffersDataProvider,
) : ScreenMapper<Unit> {
    override fun map(input: Unit, context: org.igorv8836.bdui.backend.core.ExecutionContext): BackendResult<Screen> =
        runBlocking {
            offersProvider.fetch(Unit, DataPolicy()).flatMap { offers ->
                BackendResult.success(buildHomeScreen(offers))
            }
        }
}

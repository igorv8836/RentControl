package org.igorv8836.rentcontrol.backend.module

import org.igorv8836.bdui.backend.runtime.BackendModule
import org.igorv8836.bdui.backend.runtime.BackendRegistryBuilder
import org.igorv8836.rentcontrol.backend.data.OffersDataProvider
import org.igorv8836.rentcontrol.backend.mapper.HomeScreenMapper

class DemoBackendModule : BackendModule {
    private val offersProvider = OffersDataProvider()
    private val homeMapper = HomeScreenMapper(offersProvider)

    override fun register(registry: BackendRegistryBuilder) {
        registry.dataProvider("offers", offersProvider)
        registry.mapper(screenId = "home", mapper = homeMapper)
    }
}

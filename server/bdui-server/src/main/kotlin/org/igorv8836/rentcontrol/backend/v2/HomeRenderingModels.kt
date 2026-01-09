package org.igorv8836.rentcontrol.backend.v2

import org.igorv8836.bdui.backend.runtime.engine.RenderingData
import org.igorv8836.bdui.contract.BottomBar
import org.igorv8836.rentcontrol.backend.model.OfferDto

data class HomeHeaderData(val userName: String) : RenderingData
data class HomeOffersData(val offers: List<OfferDto>) : RenderingData
data class HomeFooterData(val refreshActionId: String, val incVisitsActionId: String) : RenderingData
data class HomeScaffoldData(val bottomBar: BottomBar) : RenderingData

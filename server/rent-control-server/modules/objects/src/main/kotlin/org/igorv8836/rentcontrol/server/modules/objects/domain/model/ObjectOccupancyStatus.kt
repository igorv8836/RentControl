package org.igorv8836.rentcontrol.server.modules.objects.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ObjectOccupancyStatus {
    @SerialName("available")
    AVAILABLE,

    @SerialName("leased")
    LEASED,
}

fun ObjectOccupancyStatus.toDbValue(): String = when (this) {
    ObjectOccupancyStatus.AVAILABLE -> "available"
    ObjectOccupancyStatus.LEASED -> "leased"
}

fun objectOccupancyStatusFromDb(value: String): ObjectOccupancyStatus = when (value) {
    "available" -> ObjectOccupancyStatus.AVAILABLE
    "leased" -> ObjectOccupancyStatus.LEASED
    else -> throw IllegalArgumentException("Unknown object occupancy status: $value")
}


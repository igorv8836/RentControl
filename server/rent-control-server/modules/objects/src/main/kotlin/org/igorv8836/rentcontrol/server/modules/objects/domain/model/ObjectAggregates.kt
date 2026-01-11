package org.igorv8836.rentcontrol.server.modules.objects.domain.model

import java.time.Instant

data class ObjectAggregates(
    val defectsOpenCount: Long,
    val defectsOverdueCount: Long,
    val nextInspectionAt: Instant?,
    val lastInspectionAt: Instant?,
    val lastMeterReadingAt: Instant?,
    val expensesPlannedAmount: Double,
    val expensesActualAmount: Double,
    val expensesPendingCount: Long,
)

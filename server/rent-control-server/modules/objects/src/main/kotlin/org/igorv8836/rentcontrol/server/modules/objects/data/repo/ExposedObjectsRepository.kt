package org.igorv8836.rentcontrol.server.modules.objects.data.repo

import org.igorv8836.rentcontrol.server.foundation.db.AuditLogTable
import org.igorv8836.rentcontrol.server.foundation.db.DefectsTable
import org.igorv8836.rentcontrol.server.foundation.db.ExpensesTable
import org.igorv8836.rentcontrol.server.foundation.db.InspectionsTable
import org.igorv8836.rentcontrol.server.foundation.db.MeterReadingsTable
import org.igorv8836.rentcontrol.server.foundation.db.PropertiesTable
import org.igorv8836.rentcontrol.server.foundation.db.UsersTable
import org.igorv8836.rentcontrol.server.foundation.security.UserContext
import org.igorv8836.rentcontrol.server.foundation.security.UserRole
import org.igorv8836.rentcontrol.server.modules.objects.domain.model.ObjectActivityActor
import org.igorv8836.rentcontrol.server.modules.objects.domain.model.ObjectActivityEvent
import org.igorv8836.rentcontrol.server.modules.objects.domain.model.ObjectAggregates
import org.igorv8836.rentcontrol.server.modules.objects.domain.model.ObjectOccupancyStatus
import org.igorv8836.rentcontrol.server.modules.objects.domain.model.RentObject
import org.igorv8836.rentcontrol.server.modules.objects.domain.model.objectOccupancyStatusFromDb
import org.igorv8836.rentcontrol.server.modules.objects.domain.model.toDbValue
import org.igorv8836.rentcontrol.server.modules.objects.domain.port.CreateObjectData
import org.igorv8836.rentcontrol.server.modules.objects.domain.port.ObjectsListQuery
import org.igorv8836.rentcontrol.server.modules.objects.domain.port.ObjectsPage
import org.igorv8836.rentcontrol.server.modules.objects.domain.port.ObjectsRepository
import org.igorv8836.rentcontrol.server.modules.objects.domain.port.UpdateObjectPatch
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNull
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNotNull
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.SqlExpressionBuilder.neq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.sum
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.dao.id.EntityID
import java.math.BigDecimal
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

class ExposedObjectsRepository(
    private val database: Database,
) : ObjectsRepository {
    override suspend fun listForUser(user: UserContext, query: ObjectsListQuery): ObjectsPage =
        newSuspendedTransaction(db = database) {
            val whereClause = Op.build {
                accessibleWhere(user) and
                    archivedWhere(query.includeArchived) and
                    statusWhere(query.status) and
                    searchWhere(query.search)
            }

            val total = PropertiesTable
                .selectAll()
                .where { whereClause }
                .count()

            val offset = ((query.page - 1).toLong() * query.pageSize).coerceAtLeast(0)
            val items = PropertiesTable
                .selectAll()
                .where { whereClause }
                .orderBy(PropertiesTable.id, SortOrder.DESC)
                .limit(query.pageSize, offset)
                .map { it.toObject() }

            ObjectsPage(
                page = query.page,
                pageSize = query.pageSize,
                total = total,
                items = items,
            )
        }

    override suspend fun getForUser(user: UserContext, objectId: Long): RentObject? =
        newSuspendedTransaction(db = database) {
            PropertiesTable
                .selectAll()
                .where { (PropertiesTable.id eq objectId) and accessibleWhere(user) }
                .limit(1)
                .singleOrNull()
                ?.toObject()
        }

    override suspend fun getAggregates(objectId: Long, now: Instant): ObjectAggregates =
        newSuspendedTransaction(db = database) {
            val propertyId = EntityID(objectId, PropertiesTable)
            val nowOffset = now.toOffsetDateTime()

            val defectsOpenCount = DefectsTable
                .selectAll()
                .where { (DefectsTable.propertyId eq propertyId) and DefectsTable.resolvedAt.isNull() }
                .count()

            val defectsOverdueCount = DefectsTable
                .selectAll()
                .where {
                    (DefectsTable.propertyId eq propertyId) and
                        DefectsTable.resolvedAt.isNull() and
                        (DefectsTable.deadline less nowOffset)
                }
                .count()

            val nextInspectionAt = InspectionsTable
                .selectAll()
                .where {
                    (InspectionsTable.propertyId eq propertyId) and
                        (InspectionsTable.status eq "scheduled") and
                        (InspectionsTable.scheduledDate greaterEq nowOffset)
                }
                .orderBy(InspectionsTable.scheduledDate, SortOrder.ASC)
                .limit(1)
                .singleOrNull()
                ?.let { row -> row[InspectionsTable.scheduledDate]?.toInstant() }

            val lastInspectionAt = InspectionsTable
                .selectAll()
                .where {
                    (InspectionsTable.propertyId eq propertyId) and
                        InspectionsTable.completedDate.isNotNull()
                }
                .orderBy(InspectionsTable.completedDate, SortOrder.DESC)
                .limit(1)
                .singleOrNull()
                ?.let { row -> row[InspectionsTable.completedDate]?.toInstant() }

            val lastMeterReadingAt = MeterReadingsTable
                .selectAll()
                .where { MeterReadingsTable.propertyId eq propertyId }
                .orderBy(MeterReadingsTable.recordedAt, SortOrder.DESC)
                .limit(1)
                .singleOrNull()
                ?.let { row -> row[MeterReadingsTable.recordedAt].toInstant() }

            val monthStart = nowOffset.toLocalDate().withDayOfMonth(1)
            val monthEnd = monthStart.plusMonths(1).minusDays(1)

            val plannedSumExpr = ExpensesTable.amount.sum()
            val expensesPlannedAmount = ExpensesTable
                .select(plannedSumExpr)
                .where {
                    (ExpensesTable.propertyId eq propertyId) and
                        (ExpensesTable.type eq "plan") and
                        (ExpensesTable.status neq "canceled") and
                        (ExpensesTable.expenseDate greaterEq monthStart) and
                        (ExpensesTable.expenseDate lessEq monthEnd)
                }
                .singleOrNull()
                ?.get(plannedSumExpr)
                ?: BigDecimal.ZERO

            val actualSumExpr = ExpensesTable.amount.sum()
            val expensesActualAmount = ExpensesTable
                .select(actualSumExpr)
                .where {
                    (ExpensesTable.propertyId eq propertyId) and
                        (ExpensesTable.type eq "fact") and
                        (ExpensesTable.status neq "canceled") and
                        (ExpensesTable.expenseDate greaterEq monthStart) and
                        (ExpensesTable.expenseDate lessEq monthEnd)
                }
                .singleOrNull()
                ?.get(actualSumExpr)
                ?: BigDecimal.ZERO

            val expensesPendingCount = ExpensesTable
                .selectAll()
                .where {
                    (ExpensesTable.propertyId eq propertyId) and
                        (ExpensesTable.status eq "pending") and
                        (ExpensesTable.expenseDate greaterEq monthStart) and
                        (ExpensesTable.expenseDate lessEq monthEnd)
                }
                .count()

            ObjectAggregates(
                defectsOpenCount = defectsOpenCount,
                defectsOverdueCount = defectsOverdueCount,
                nextInspectionAt = nextInspectionAt,
                lastInspectionAt = lastInspectionAt,
                lastMeterReadingAt = lastMeterReadingAt,
                expensesPlannedAmount = expensesPlannedAmount.toDouble(),
                expensesActualAmount = expensesActualAmount.toDouble(),
                expensesPendingCount = expensesPendingCount,
            )
        }

    override suspend fun listActivity(objectId: Long, limit: Int): List<ObjectActivityEvent> =
        newSuspendedTransaction(db = database) {
            val rows = AuditLogTable
                .selectAll()
                .where { (AuditLogTable.entityType eq ENTITY_TYPE_OBJECT) and (AuditLogTable.entityId eq objectId) }
                .orderBy(AuditLogTable.id, SortOrder.DESC)
                .limit(limit)
                .map { row ->
                    AuditRow(
                        id = row[AuditLogTable.id].value,
                        createdAt = row[AuditLogTable.createdAt].toInstant(),
                        userId = row[AuditLogTable.userId]?.value,
                        action = row[AuditLogTable.action],
                    )
                }

            val actorIds = rows.mapNotNull { it.userId }.distinct()
            val actorsById: Map<Long, ObjectActivityActor> = if (actorIds.isEmpty()) {
                emptyMap()
            } else {
                UsersTable
                    .selectAll()
                    .where { UsersTable.id inList actorIds }
                    .associate { row ->
                        val id = row[UsersTable.id].value
                        id to ObjectActivityActor(
                            id = id,
                            fullName = row[UsersTable.fullName],
                            email = row[UsersTable.email],
                            phone = row[UsersTable.phone],
                        )
                    }
            }

            rows.map { row ->
                ObjectActivityEvent(
                    id = row.id,
                    createdAt = row.createdAt,
                    actor = row.userId?.let(actorsById::get),
                    action = row.action,
                )
            }
        }

    override suspend fun create(data: CreateObjectData): RentObject = newSuspendedTransaction(db = database) {
        val now = OffsetDateTime.now(ZoneOffset.UTC)
        val id = PropertiesTable.insertAndGetId {
            it[address] = data.address
            it[type] = data.type
            it[area] = data.area?.let(BigDecimal::valueOf)
            it[notes] = data.notes
            it[status] = data.status.toDbValue()
            it[ownerId] = EntityID(data.ownerId, UsersTable)
            it[tenantId] = data.tenantId?.let { tenantId -> EntityID(tenantId, UsersTable) }
            it[archivedAt] = null
            it[createdAt] = now
            it[updatedAt] = now
        }.value

        insertAudit(
            userId = data.createdByUserId,
            objectId = id,
            action = ACTION_OBJECT_CREATED,
        )

        PropertiesTable
            .selectAll()
            .where { PropertiesTable.id eq id }
            .limit(1)
            .singleOrNull()
            ?.toObject()
            ?: error("Failed to load created object")
    }

    override suspend fun updateForUser(user: UserContext, objectId: Long, patch: UpdateObjectPatch): RentObject? =
        newSuspendedTransaction(db = database) {
            val now = OffsetDateTime.now(ZoneOffset.UTC)
            val updated = PropertiesTable.update(
                where = { (PropertiesTable.id eq objectId) and accessibleWhere(user) },
            ) { row ->
                patch.address?.let { row[PropertiesTable.address] = it }
                patch.type?.let { row[PropertiesTable.type] = it }
                patch.notes?.let { row[PropertiesTable.notes] = it }
                patch.area?.let { row[PropertiesTable.area] = BigDecimal.valueOf(it) }
                patch.status?.let { row[PropertiesTable.status] = it.toDbValue() }
                if (patch.tenantId != null) {
                    row[PropertiesTable.tenantId] = EntityID(patch.tenantId, UsersTable)
                }
                row[PropertiesTable.updatedAt] = now
            }

            if (updated == 0) {
                return@newSuspendedTransaction null
            }

            insertAudit(
                userId = user.userId,
                objectId = objectId,
                action = ACTION_OBJECT_UPDATED,
            )

            getForUser(user, objectId)
        }

    override suspend fun linkTenantForUser(user: UserContext, objectId: Long, tenantId: Long): RentObject? =
        newSuspendedTransaction(db = database) {
            val now = OffsetDateTime.now(ZoneOffset.UTC)
            val updated = PropertiesTable.update(
                where = { (PropertiesTable.id eq objectId) and accessibleWhere(user) },
            ) { row ->
                row[PropertiesTable.tenantId] = EntityID(tenantId, UsersTable)
                row[PropertiesTable.status] = ObjectOccupancyStatus.LEASED.toDbValue()
                row[PropertiesTable.updatedAt] = now
            }

            if (updated == 0) {
                return@newSuspendedTransaction null
            }

            insertAudit(
                userId = user.userId,
                objectId = objectId,
                action = ACTION_TENANT_LINKED,
            )

            getForUser(user, objectId)
        }

    override suspend fun unlinkTenantForUser(user: UserContext, objectId: Long): RentObject? =
        newSuspendedTransaction(db = database) {
            val now = OffsetDateTime.now(ZoneOffset.UTC)
            val updated = PropertiesTable.update(
                where = { (PropertiesTable.id eq objectId) and accessibleWhere(user) },
            ) { row ->
                row[PropertiesTable.tenantId] = null
                row[PropertiesTable.status] = ObjectOccupancyStatus.AVAILABLE.toDbValue()
                row[PropertiesTable.updatedAt] = now
            }

            if (updated == 0) {
                return@newSuspendedTransaction null
            }

            insertAudit(
                userId = user.userId,
                objectId = objectId,
                action = ACTION_TENANT_UNLINKED,
            )

            getForUser(user, objectId)
        }

    override suspend fun setArchivedForUser(user: UserContext, objectId: Long, archived: Boolean): RentObject? =
        newSuspendedTransaction(db = database) {
            val now = OffsetDateTime.now(ZoneOffset.UTC)
            val archivedAtValue = if (archived) now else null

            val updated = PropertiesTable.update(
                where = { (PropertiesTable.id eq objectId) and accessibleWhere(user) },
            ) { row ->
                row[PropertiesTable.archivedAt] = archivedAtValue
                row[PropertiesTable.updatedAt] = now
            }

            if (updated == 0) {
                return@newSuspendedTransaction null
            }

            insertAudit(
                userId = user.userId,
                objectId = objectId,
                action = if (archived) ACTION_OBJECT_ARCHIVED else ACTION_OBJECT_UNARCHIVED,
            )

            getForUser(user, objectId)
        }

    private fun ResultRow.toObject(): RentObject = RentObject(
        id = this[PropertiesTable.id].value,
        address = this[PropertiesTable.address],
        type = this[PropertiesTable.type],
        area = this[PropertiesTable.area]?.toDouble(),
        status = objectOccupancyStatusFromDb(this[PropertiesTable.status]),
        notes = this[PropertiesTable.notes],
        ownerId = this[PropertiesTable.ownerId].value,
        tenantId = this[PropertiesTable.tenantId]?.value,
        archivedAt = this[PropertiesTable.archivedAt]?.toInstant(),
        createdAt = this[PropertiesTable.createdAt].toInstant(),
        updatedAt = this[PropertiesTable.updatedAt].toInstant(),
    )

    private fun accessibleWhere(user: UserContext): Op<Boolean> = when (user.role) {
        UserRole.ADMIN, UserRole.INSPECTOR -> Op.TRUE
        UserRole.LANDLORD -> PropertiesTable.ownerId eq user.userId
        UserRole.TENANT -> PropertiesTable.tenantId eq user.userId
    }

    private fun archivedWhere(includeArchived: Boolean): Op<Boolean> =
        if (includeArchived) {
            Op.TRUE
        } else {
            PropertiesTable.archivedAt.isNull()
        }

    private fun statusWhere(status: ObjectOccupancyStatus?): Op<Boolean> =
        status?.let { PropertiesTable.status eq it.toDbValue() } ?: Op.TRUE

    private fun searchWhere(search: String?): Op<Boolean> =
        search?.let { PropertiesTable.address like "%$it%" } ?: Op.TRUE

    private fun Instant.toOffsetDateTime(): OffsetDateTime = OffsetDateTime.ofInstant(this, ZoneOffset.UTC)

    private fun insertAudit(
        userId: Long,
        objectId: Long,
        action: String,
    ) {
        AuditLogTable.insert { row ->
            row[AuditLogTable.userId] = EntityID(userId, UsersTable)
            row[AuditLogTable.entityType] = ENTITY_TYPE_OBJECT
            row[AuditLogTable.entityId] = objectId
            row[AuditLogTable.action] = action
        }
    }

    private data class AuditRow(
        val id: Long,
        val createdAt: Instant,
        val userId: Long?,
        val action: String,
    )

    private companion object {
        private const val ENTITY_TYPE_OBJECT = "object"
        private const val ACTION_OBJECT_CREATED = "object_created"
        private const val ACTION_OBJECT_UPDATED = "object_updated"
        private const val ACTION_OBJECT_ARCHIVED = "object_archived"
        private const val ACTION_OBJECT_UNARCHIVED = "object_unarchived"
        private const val ACTION_TENANT_LINKED = "tenant_linked"
        private const val ACTION_TENANT_UNLINKED = "tenant_unlinked"
    }
}

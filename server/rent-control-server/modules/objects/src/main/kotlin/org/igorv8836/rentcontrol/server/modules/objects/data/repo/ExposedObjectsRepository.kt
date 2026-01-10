package org.igorv8836.rentcontrol.server.modules.objects.data.repo

import org.igorv8836.rentcontrol.server.foundation.db.PropertiesTable
import org.igorv8836.rentcontrol.server.foundation.db.UsersTable
import org.igorv8836.rentcontrol.server.foundation.security.UserContext
import org.igorv8836.rentcontrol.server.foundation.security.UserRole
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
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNull
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.dao.id.EntityID
import java.math.BigDecimal
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
}

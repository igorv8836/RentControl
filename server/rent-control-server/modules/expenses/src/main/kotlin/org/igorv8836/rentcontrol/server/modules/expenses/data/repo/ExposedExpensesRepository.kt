package org.igorv8836.rentcontrol.server.modules.expenses.data.repo

import org.igorv8836.rentcontrol.server.foundation.db.ExpenseApprovalsTable
import org.igorv8836.rentcontrol.server.foundation.db.ExpensesTable
import org.igorv8836.rentcontrol.server.foundation.db.MediaFilesTable
import org.igorv8836.rentcontrol.server.foundation.db.PropertiesTable
import org.igorv8836.rentcontrol.server.foundation.db.UsersTable
import org.igorv8836.rentcontrol.server.foundation.security.UserContext
import org.igorv8836.rentcontrol.server.foundation.security.UserRole
import org.igorv8836.rentcontrol.server.modules.expenses.domain.model.Expense
import org.igorv8836.rentcontrol.server.modules.expenses.domain.model.ExpenseApproval
import org.igorv8836.rentcontrol.server.modules.expenses.domain.model.ExpenseApprovalStatus
import org.igorv8836.rentcontrol.server.modules.expenses.domain.model.ExpenseAttachment
import org.igorv8836.rentcontrol.server.modules.expenses.domain.model.ExpenseDetails
import org.igorv8836.rentcontrol.server.modules.expenses.domain.model.ExpenseStatus
import org.igorv8836.rentcontrol.server.modules.expenses.domain.model.ExpenseUserSummary
import org.igorv8836.rentcontrol.server.modules.expenses.domain.model.ExpenseType
import org.igorv8836.rentcontrol.server.modules.expenses.domain.model.expenseApprovalStatusFromDb
import org.igorv8836.rentcontrol.server.modules.expenses.domain.model.expenseStatusFromDb
import org.igorv8836.rentcontrol.server.modules.expenses.domain.model.expenseTypeFromDb
import org.igorv8836.rentcontrol.server.modules.expenses.domain.model.toDbValue
import org.igorv8836.rentcontrol.server.modules.expenses.domain.port.CreateExpenseData
import org.igorv8836.rentcontrol.server.modules.expenses.domain.port.ExpensesListQuery
import org.igorv8836.rentcontrol.server.modules.expenses.domain.port.ExpensesPage
import org.igorv8836.rentcontrol.server.modules.expenses.domain.port.ExpensesRepository
import org.igorv8836.rentcontrol.server.modules.expenses.domain.port.UpdateExpensePatch
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.SqlExpressionBuilder.neq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.countDistinct
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.dao.id.EntityID
import java.math.BigDecimal
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

class ExposedExpensesRepository(
    private val database: Database,
) : ExpensesRepository {
    override suspend fun listForUser(user: UserContext, query: ExpensesListQuery): ExpensesPage =
        newSuspendedTransaction(db = database) {
            val join = ExpensesTable
                .join(PropertiesTable, JoinType.INNER, additionalConstraint = { ExpensesTable.propertyId eq PropertiesTable.id })
                .join(ExpenseApprovalsTable, JoinType.LEFT, additionalConstraint = { ExpenseApprovalsTable.expenseId eq ExpensesTable.id })

            val whereClause = Op.build {
                visibilityWhere(user) and
                    objectWhere(query.objectId) and
                    categoryWhere(query.category) and
                    typeWhere(query.type) and
                    statusWhere(query.status) and
                    dateWhere(query.fromDate, query.toDate) and
                    searchWhere(query.search)
            }

            val totalExpr = ExpensesTable.id.countDistinct()
            val total = join
                .select(totalExpr)
                .where { whereClause }
                .single()[totalExpr]

            val offset = ((query.page - 1).toLong() * query.pageSize).coerceAtLeast(0)
            val ids = join
                .select(ExpensesTable.id)
                .where { whereClause }
                .withDistinct()
                .orderBy(ExpensesTable.id, SortOrder.DESC)
                .limit(query.pageSize, offset)
                .map { it[ExpensesTable.id].value }

            val items = if (ids.isEmpty()) {
                emptyList()
            } else {
                ExpensesTable
                    .join(PropertiesTable, JoinType.INNER, additionalConstraint = { ExpensesTable.propertyId eq PropertiesTable.id })
                    .selectAll()
                    .where { ExpensesTable.id inList ids }
                    .orderBy(ExpensesTable.id, SortOrder.DESC)
                    .map { it.toExpense() }
            }

            ExpensesPage(
                page = query.page,
                pageSize = query.pageSize,
                total = total,
                items = items,
            )
        }

    override suspend fun getForUser(user: UserContext, expenseId: Long): ExpenseDetails? =
        newSuspendedTransaction(db = database) {
            val expenseRow = ExpensesTable
                .join(PropertiesTable, JoinType.INNER, additionalConstraint = { ExpensesTable.propertyId eq PropertiesTable.id })
                .selectAll()
                .where { ExpensesTable.id eq expenseId }
                .limit(1)
                .singleOrNull()
                ?: return@newSuspendedTransaction null

            val expense = expenseRow.toExpense()
            if (!isVisible(user, expenseId, expense.authorId)) {
                return@newSuspendedTransaction null
            }

            expense.toDetails(
                approvals = loadApprovals(expenseId),
                attachments = loadAttachments(expenseId),
            )
        }

    override suspend fun createForUser(user: UserContext, data: CreateExpenseData): ExpenseDetails? =
        newSuspendedTransaction(db = database) {
            val property = loadAccessibleProperty(user, data.objectId) ?: return@newSuspendedTransaction null
            val now = data.now.toOffsetDateTime()

            val expenseId = ExpensesTable.insertAndGetId { row ->
                row[propertyId] = property.id
                row[authorId] = EntityID(user.userId, UsersTable)
                row[category] = data.category
                row[type] = data.type.toDbValue()
                row[amount] = BigDecimal.valueOf(data.amount)
                row[expenseDate] = data.expenseDate
                row[description] = data.description
                row[status] = ExpenseStatus.DRAFT.toDbValue()
                row[submittedAt] = null
                row[createdAt] = now
                row[updatedAt] = now
            }.value

            replaceApprovers(expenseId, data.approverIds)
            bindAttachments(expenseId, user.userId, data.attachmentIds)

            val created = Expense(
                id = expenseId,
                objectId = property.id.value,
                objectAddress = property.address,
                authorId = user.userId,
                category = data.category,
                type = data.type,
                amount = data.amount,
                expenseDate = data.expenseDate,
                description = data.description,
                status = ExpenseStatus.DRAFT,
                submittedAt = null,
                createdAt = data.now,
                updatedAt = data.now,
            )

            created.toDetails(
                approvals = loadApprovals(expenseId),
                attachments = loadAttachments(expenseId),
            )
        }

    override suspend fun updateForAuthor(user: UserContext, expenseId: Long, patch: UpdateExpensePatch): ExpenseDetails? =
        newSuspendedTransaction(db = database) {
            val existingRow = ExpensesTable
                .join(PropertiesTable, JoinType.INNER, additionalConstraint = { ExpensesTable.propertyId eq PropertiesTable.id })
                .selectAll()
                .where { (ExpensesTable.id eq expenseId) and (ExpensesTable.authorId eq user.userId) }
                .limit(1)
                .singleOrNull()
                ?: return@newSuspendedTransaction null

            val existing = existingRow.toExpense()
            val now = patch.now.toOffsetDateTime()

            val updated = ExpensesTable.update(
                where = { (ExpensesTable.id eq expenseId) and (ExpensesTable.authorId eq user.userId) },
            ) { row ->
                patch.category?.let { row[category] = it }
                patch.type?.let { row[type] = it.toDbValue() }
                patch.amount?.let { row[amount] = BigDecimal.valueOf(it) }
                patch.expenseDate?.let { row[expenseDate] = it }
                patch.description?.let { row[description] = it }
                row[updatedAt] = now
            }

            if (updated == 0) {
                return@newSuspendedTransaction null
            }

            patch.approverIds?.let { replaceApprovers(expenseId, it) }
            patch.attachmentIds?.let { bindAttachments(expenseId, user.userId, it) }

            val reloaded = getForUser(user, expenseId) ?: existing.toDetails(emptyList(), emptyList())
            reloaded
        }

    override suspend fun submitForAuthor(user: UserContext, expenseId: Long, now: Instant): ExpenseDetails? =
        newSuspendedTransaction(db = database) {
            val approvalsCount = ExpenseApprovalsTable
                .selectAll()
                .where { ExpenseApprovalsTable.expenseId eq expenseId }
                .count()

            val newStatus = if (approvalsCount == 0L) ExpenseStatus.APPROVED else ExpenseStatus.PENDING

            val updated = ExpensesTable.update(
                where = {
                    (ExpensesTable.id eq expenseId) and
                        (ExpensesTable.authorId eq user.userId) and
                        (ExpensesTable.status eq ExpenseStatus.DRAFT.toDbValue())
                },
            ) { row ->
                row[status] = newStatus.toDbValue()
                row[submittedAt] = now.toOffsetDateTime()
                row[updatedAt] = now.toOffsetDateTime()
            }

            if (updated == 0) {
                return@newSuspendedTransaction null
            }

            getForUser(user, expenseId)
        }

    override suspend fun approve(user: UserContext, expenseId: Long, now: Instant): ExpenseDetails? =
        newSuspendedTransaction(db = database) {
            val expense = ExpensesTable
                .selectAll()
                .where { ExpensesTable.id eq expenseId }
                .limit(1)
                .singleOrNull()
                ?: return@newSuspendedTransaction null

            if (expense[ExpensesTable.status] != ExpenseStatus.PENDING.toDbValue()) {
                return@newSuspendedTransaction null
            }

            val updatedApproval = ExpenseApprovalsTable.update(
                where = {
                    (ExpenseApprovalsTable.expenseId eq expenseId) and
                        (ExpenseApprovalsTable.approverId eq user.userId) and
                        (ExpenseApprovalsTable.status eq ExpenseApprovalStatus.PENDING.toDbValue())
                },
            ) { row ->
                row[status] = ExpenseApprovalStatus.APPROVED.toDbValue()
                row[decidedAt] = now.toOffsetDateTime()
            }

            if (updatedApproval == 0) {
                return@newSuspendedTransaction null
            }

            val remaining = ExpenseApprovalsTable
                .selectAll()
                .where {
                    (ExpenseApprovalsTable.expenseId eq expenseId) and
                        (ExpenseApprovalsTable.status eq ExpenseApprovalStatus.PENDING.toDbValue())
                }
                .count()

            if (remaining == 0L) {
                ExpensesTable.update(
                    where = { ExpensesTable.id eq expenseId },
                ) { row ->
                    row[status] = ExpenseStatus.APPROVED.toDbValue()
                    row[updatedAt] = now.toOffsetDateTime()
                }
            }

            getForUser(user, expenseId)
        }

    override suspend fun reject(user: UserContext, expenseId: Long, comment: String, now: Instant): ExpenseDetails? =
        newSuspendedTransaction(db = database) {
            val expense = ExpensesTable
                .selectAll()
                .where { ExpensesTable.id eq expenseId }
                .limit(1)
                .singleOrNull()
                ?: return@newSuspendedTransaction null

            if (expense[ExpensesTable.status] != ExpenseStatus.PENDING.toDbValue()) {
                return@newSuspendedTransaction null
            }

            val updatedApproval = ExpenseApprovalsTable.update(
                where = {
                    (ExpenseApprovalsTable.expenseId eq expenseId) and
                        (ExpenseApprovalsTable.approverId eq user.userId) and
                        (ExpenseApprovalsTable.status eq ExpenseApprovalStatus.PENDING.toDbValue())
                },
            ) { row ->
                row[status] = ExpenseApprovalStatus.REJECTED.toDbValue()
                row[decidedAt] = now.toOffsetDateTime()
                row[ExpenseApprovalsTable.comment] = comment
            }

            if (updatedApproval == 0) {
                return@newSuspendedTransaction null
            }

            ExpensesTable.update(
                where = { ExpensesTable.id eq expenseId },
            ) { row ->
                row[status] = ExpenseStatus.REJECTED.toDbValue()
                row[updatedAt] = now.toOffsetDateTime()
            }

            getForUser(user, expenseId)
        }

    override suspend fun cancelForAuthor(user: UserContext, expenseId: Long, now: Instant): ExpenseDetails? =
        newSuspendedTransaction(db = database) {
            val updated = ExpensesTable.update(
                where = {
                    (ExpensesTable.id eq expenseId) and
                        (ExpensesTable.authorId eq user.userId) and
                        (ExpensesTable.status neq ExpenseStatus.CANCELED.toDbValue()) and
                        (ExpensesTable.status neq ExpenseStatus.APPROVED.toDbValue())
                },
            ) { row ->
                row[status] = ExpenseStatus.CANCELED.toDbValue()
                row[updatedAt] = now.toOffsetDateTime()
            }

            if (updated == 0) {
                return@newSuspendedTransaction null
            }

            getForUser(user, expenseId)
        }

    private fun visibilityWhere(user: UserContext): Op<Boolean> =
        (ExpensesTable.authorId eq user.userId) or (ExpenseApprovalsTable.approverId eq user.userId)

    private fun objectWhere(objectId: Long?): Op<Boolean> =
        objectId?.let { ExpensesTable.propertyId eq it } ?: Op.TRUE

    private fun categoryWhere(category: String?): Op<Boolean> =
        category?.trim()?.takeIf { it.isNotBlank() }?.let { ExpensesTable.category eq it } ?: Op.TRUE

    private fun typeWhere(type: ExpenseType?): Op<Boolean> =
        type?.let { ExpensesTable.type eq it.toDbValue() } ?: Op.TRUE

    private fun statusWhere(status: ExpenseStatus?): Op<Boolean> =
        status?.let { ExpensesTable.status eq it.toDbValue() } ?: Op.TRUE

    private fun dateWhere(fromDate: java.time.LocalDate?, toDate: java.time.LocalDate?): Op<Boolean> {
        val from = fromDate?.let { ExpensesTable.expenseDate greaterEq it } ?: Op.TRUE
        val to = toDate?.let { ExpensesTable.expenseDate lessEq it } ?: Op.TRUE
        return from and to
    }

    private fun searchWhere(search: String?): Op<Boolean> =
        search?.trim()?.takeIf { it.isNotBlank() }?.let {
            val pattern = "%$it%"
            ExpensesTable.description like pattern
        } ?: Op.TRUE

    private fun ResultRow.toExpense(): Expense = Expense(
        id = this[ExpensesTable.id].value,
        objectId = this[ExpensesTable.propertyId].value,
        objectAddress = this[PropertiesTable.address],
        authorId = this[ExpensesTable.authorId].value,
        category = this[ExpensesTable.category],
        type = expenseTypeFromDb(this[ExpensesTable.type]),
        amount = this[ExpensesTable.amount].toDouble(),
        expenseDate = this[ExpensesTable.expenseDate],
        description = this[ExpensesTable.description],
        status = expenseStatusFromDb(this[ExpensesTable.status]),
        submittedAt = this[ExpensesTable.submittedAt]?.toInstant(),
        createdAt = this[ExpensesTable.createdAt].toInstant(),
        updatedAt = this[ExpensesTable.updatedAt].toInstant(),
    )

    private fun Expense.toDetails(
        approvals: List<ExpenseApproval>,
        attachments: List<ExpenseAttachment>,
    ): ExpenseDetails = ExpenseDetails(
        expense = this,
        approvals = approvals,
        attachments = attachments,
    )

    private fun loadApprovals(expenseId: Long): List<ExpenseApproval> {
        return ExpenseApprovalsTable
            .join(UsersTable, JoinType.INNER, additionalConstraint = { ExpenseApprovalsTable.approverId eq UsersTable.id })
            .selectAll()
            .where { ExpenseApprovalsTable.expenseId eq expenseId }
            .orderBy(ExpenseApprovalsTable.sortOrder, SortOrder.ASC)
            .map { row ->
                ExpenseApproval(
                    id = row[ExpenseApprovalsTable.id].value,
                    approver = ExpenseUserSummary(
                        id = row[UsersTable.id].value,
                        fullName = row[UsersTable.fullName],
                        email = row[UsersTable.email],
                        phone = row[UsersTable.phone],
                    ),
                    sortOrder = row[ExpenseApprovalsTable.sortOrder],
                    status = expenseApprovalStatusFromDb(row[ExpenseApprovalsTable.status]),
                    decidedAt = row[ExpenseApprovalsTable.decidedAt]?.toInstant(),
                    comment = row[ExpenseApprovalsTable.comment],
                )
            }
    }

    private fun loadAttachments(expenseId: Long): List<ExpenseAttachment> {
        return MediaFilesTable
            .selectAll()
            .where { (MediaFilesTable.entityType eq ENTITY_TYPE_EXPENSE) and (MediaFilesTable.entityId eq expenseId) }
            .orderBy(MediaFilesTable.id, SortOrder.DESC)
            .map { row ->
                ExpenseAttachment(
                    id = row[MediaFilesTable.id].value,
                    url = row[MediaFilesTable.fileUrl],
                    type = row[MediaFilesTable.fileType],
                    createdAt = row[MediaFilesTable.createdAt].toInstant(),
                )
            }
    }

    private data class PropertyRow(
        val id: org.jetbrains.exposed.dao.id.EntityID<Long>,
        val address: String,
    )

    private fun loadAccessibleProperty(user: UserContext, objectId: Long): PropertyRow? {
        val where = when (user.role) {
            UserRole.ADMIN, UserRole.INSPECTOR -> PropertiesTable.id eq objectId
            UserRole.LANDLORD -> (PropertiesTable.id eq objectId) and (PropertiesTable.ownerId eq user.userId)
            UserRole.TENANT -> (PropertiesTable.id eq objectId) and (PropertiesTable.tenantId eq user.userId)
        }

        return PropertiesTable
            .selectAll()
            .where { where }
            .limit(1)
            .singleOrNull()
            ?.let { row ->
                PropertyRow(
                    id = row[PropertiesTable.id],
                    address = row[PropertiesTable.address],
                )
            }
    }

    private fun isVisible(user: UserContext, expenseId: Long, authorId: Long): Boolean {
        if (authorId == user.userId) {
            return true
        }

        val exists = ExpenseApprovalsTable
            .selectAll()
            .where { (ExpenseApprovalsTable.expenseId eq expenseId) and (ExpenseApprovalsTable.approverId eq user.userId) }
            .limit(1)
            .singleOrNull() != null

        return exists
    }

    private fun replaceApprovers(expenseId: Long, approverIds: List<Long>) {
        ExpenseApprovalsTable.deleteWhere { ExpenseApprovalsTable.expenseId eq expenseId }

        approverIds.forEachIndexed { index, approverId ->
            ExpenseApprovalsTable.insert { row ->
                row[ExpenseApprovalsTable.expenseId] = org.jetbrains.exposed.dao.id.EntityID(expenseId, ExpensesTable)
                row[ExpenseApprovalsTable.approverId] = org.jetbrains.exposed.dao.id.EntityID(approverId, UsersTable)
                row[ExpenseApprovalsTable.sortOrder] = index
                row[ExpenseApprovalsTable.status] = ExpenseApprovalStatus.PENDING.toDbValue()
                row[ExpenseApprovalsTable.decidedAt] = null
                row[ExpenseApprovalsTable.comment] = null
            }
        }
    }

    private fun bindAttachments(expenseId: Long, ownerId: Long, attachmentIds: List<Long>) {
        MediaFilesTable.update(
            where = {
                (MediaFilesTable.entityType eq ENTITY_TYPE_EXPENSE) and
                    (MediaFilesTable.entityId eq expenseId) and
                    (MediaFilesTable.ownerId eq ownerId)
            },
        ) { row ->
            row[MediaFilesTable.entityType] = ENTITY_TYPE_UNBOUND
            row[MediaFilesTable.entityId] = 0
        }

        if (attachmentIds.isEmpty()) {
            return
        }

        MediaFilesTable.update(
            where = { (MediaFilesTable.id inList attachmentIds) and (MediaFilesTable.ownerId eq ownerId) },
        ) { row ->
            row[MediaFilesTable.entityType] = ENTITY_TYPE_EXPENSE
            row[MediaFilesTable.entityId] = expenseId
        }
    }

    private fun Instant.toOffsetDateTime(): OffsetDateTime = OffsetDateTime.ofInstant(this, ZoneOffset.UTC)

    private companion object {
        private const val ENTITY_TYPE_EXPENSE = "expense"
        private const val ENTITY_TYPE_UNBOUND = "unbound"
    }
}

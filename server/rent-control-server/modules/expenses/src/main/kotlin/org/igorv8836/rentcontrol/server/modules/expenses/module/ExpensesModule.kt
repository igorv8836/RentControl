package org.igorv8836.rentcontrol.server.modules.expenses.module

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.igorv8836.rentcontrol.server.foundation.errors.ApiException
import org.igorv8836.rentcontrol.server.foundation.security.userContext
import org.igorv8836.rentcontrol.server.modules.expenses.api.dto.CreateExpenseRequest
import org.igorv8836.rentcontrol.server.modules.expenses.api.dto.ExpenseApprovalItem
import org.igorv8836.rentcontrol.server.modules.expenses.api.dto.ExpenseAttachmentItem
import org.igorv8836.rentcontrol.server.modules.expenses.api.dto.ExpenseDetailsResponse
import org.igorv8836.rentcontrol.server.modules.expenses.api.dto.ExpenseListItem
import org.igorv8836.rentcontrol.server.modules.expenses.api.dto.ExpenseUserSummary
import org.igorv8836.rentcontrol.server.modules.expenses.api.dto.ExpensesListResponse
import org.igorv8836.rentcontrol.server.modules.expenses.api.dto.RejectExpenseRequest
import org.igorv8836.rentcontrol.server.modules.expenses.api.dto.UpdateExpenseRequest
import org.igorv8836.rentcontrol.server.modules.expenses.domain.model.Expense
import org.igorv8836.rentcontrol.server.modules.expenses.domain.model.ExpenseApproval
import org.igorv8836.rentcontrol.server.modules.expenses.domain.model.ExpenseAttachment
import org.igorv8836.rentcontrol.server.modules.expenses.domain.model.ExpenseDetails
import org.igorv8836.rentcontrol.server.modules.expenses.domain.model.ExpenseUserSummary as DomainUserSummary
import org.igorv8836.rentcontrol.server.modules.expenses.domain.port.ExpensesListQuery
import org.igorv8836.rentcontrol.server.modules.expenses.domain.service.ExpensesService
import org.igorv8836.rentcontrol.server.modules.expenses.domain.service.UpdateExpensePatchInput
import java.time.Instant
import java.time.LocalDate

fun Route.expensesModule(expensesService: ExpensesService) {
    route("/expenses") {
        get {
            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
            val pageSize = call.request.queryParameters["pageSize"]?.toIntOrNull() ?: 20
            val search = call.request.queryParameters["q"]?.trim()?.takeIf { it.isNotBlank() }
            val objectId = call.request.queryParameters["objectId"]?.toLongOrNull()
            val category = call.request.queryParameters["category"]?.trim()?.takeIf { it.isNotBlank() }
            val type = call.request.queryParameters["type"]?.trim()?.takeIf { it.isNotBlank() }?.let(::parseType)
            val status = call.request.queryParameters["status"]?.trim()?.takeIf { it.isNotBlank() }?.let(::parseStatus)

            val from = call.request.queryParameters["from"]?.trim()?.takeIf { it.isNotBlank() }?.let(::parseDate)
            val to = call.request.queryParameters["to"]?.trim()?.takeIf { it.isNotBlank() }?.let(::parseDate)

            val result = expensesService.listExpenses(
                user = call.userContext,
                query = ExpensesListQuery(
                    search = search,
                    objectId = objectId,
                    category = category,
                    type = type,
                    status = status,
                    fromDate = from,
                    toDate = to,
                    page = page,
                    pageSize = pageSize,
                ),
            )

            call.respond(
                ExpensesListResponse(
                    page = result.page,
                    pageSize = result.pageSize,
                    total = result.total,
                    items = result.items.map { it.toListItem() },
                ),
            )
        }

        post {
            val request = call.receive<CreateExpenseRequest>()
            val created = expensesService.createExpense(
                user = call.userContext,
                objectId = request.objectId,
                category = request.category,
                type = request.type,
                amount = request.amount,
                expenseDate = parseDate(request.expenseDate),
                description = request.description,
                attachmentIds = request.attachmentIds,
                approverIds = request.approverIds,
            )
            call.respond(HttpStatusCode.Created, created.toDetailsResponse())
        }

        route("/{expenseId}") {
            get {
                val expenseId = call.expenseIdOrThrow()
                val details = expensesService.getExpense(call.userContext, expenseId)
                call.respond(details.toDetailsResponse())
            }

            patch {
                val expenseId = call.expenseIdOrThrow()
                val request = call.receive<UpdateExpenseRequest>()
                val updated = expensesService.updateExpense(
                    user = call.userContext,
                    expenseId = expenseId,
                    patch = UpdateExpensePatchInput(
                        category = request.category,
                        type = request.type,
                        amount = request.amount,
                        expenseDate = request.expenseDate?.let(::parseDate),
                        description = request.description,
                        attachmentIds = request.attachmentIds,
                        approverIds = request.approverIds,
                    ),
                )
                call.respond(updated.toDetailsResponse())
            }

            post("/submit") {
                val expenseId = call.expenseIdOrThrow()
                val updated = expensesService.submitExpense(call.userContext, expenseId)
                call.respond(updated.toDetailsResponse())
            }

            post("/approve") {
                val expenseId = call.expenseIdOrThrow()
                val updated = expensesService.approveExpense(call.userContext, expenseId)
                call.respond(updated.toDetailsResponse())
            }

            post("/reject") {
                val expenseId = call.expenseIdOrThrow()
                val request = call.receive<RejectExpenseRequest>()
                val updated = expensesService.rejectExpense(call.userContext, expenseId, request.comment)
                call.respond(updated.toDetailsResponse())
            }

            post("/cancel") {
                val expenseId = call.expenseIdOrThrow()
                val updated = expensesService.cancelExpense(call.userContext, expenseId)
                call.respond(updated.toDetailsResponse())
            }
        }
    }
}

private fun io.ktor.server.application.ApplicationCall.expenseIdOrThrow(): Long =
    parameters["expenseId"]?.toLongOrNull()
        ?: throw ApiException(
            status = HttpStatusCode.BadRequest,
            code = "invalid_argument",
            message = "Invalid expenseId",
        )

private fun parseDate(value: String): LocalDate =
    runCatching { LocalDate.parse(value) }
        .getOrNull()
        ?: throw ApiException(
            status = HttpStatusCode.BadRequest,
            code = "invalid_argument",
            message = "Invalid date",
        )

private fun parseType(value: String): org.igorv8836.rentcontrol.server.modules.expenses.domain.model.ExpenseType =
    runCatching { org.igorv8836.rentcontrol.server.modules.expenses.domain.model.ExpenseType.valueOf(value.uppercase()) }
        .getOrNull()
        ?: throw ApiException(
            status = HttpStatusCode.BadRequest,
            code = "invalid_argument",
            message = "Invalid type",
        )

private fun parseStatus(value: String): org.igorv8836.rentcontrol.server.modules.expenses.domain.model.ExpenseStatus =
    runCatching { org.igorv8836.rentcontrol.server.modules.expenses.domain.model.ExpenseStatus.valueOf(value.uppercase()) }
        .getOrNull()
        ?: throw ApiException(
            status = HttpStatusCode.BadRequest,
            code = "invalid_argument",
            message = "Invalid status",
        )

private fun Expense.toListItem(): ExpenseListItem =
    ExpenseListItem(
        id = id,
        objectId = objectId,
        objectAddress = objectAddress,
        category = category,
        type = type,
        amount = amount,
        expenseDate = expenseDate.toString(),
        description = description,
        status = status,
    )

private fun ExpenseDetails.toDetailsResponse(): ExpenseDetailsResponse =
    ExpenseDetailsResponse(
        id = expense.id,
        objectId = expense.objectId,
        objectAddress = expense.objectAddress,
        authorId = expense.authorId,
        category = expense.category,
        type = expense.type,
        amount = expense.amount,
        expenseDate = expense.expenseDate.toString(),
        description = expense.description,
        status = expense.status,
        submittedAt = expense.submittedAt?.toIsoString(),
        approvals = approvals.map { it.toApprovalItem() },
        attachments = attachments.map { it.toAttachmentItem() },
    )

private fun ExpenseApproval.toApprovalItem(): ExpenseApprovalItem =
    ExpenseApprovalItem(
        id = id,
        approver = approver.toDto(),
        sortOrder = sortOrder,
        status = status,
        decidedAt = decidedAt?.toIsoString(),
        comment = comment,
    )

private fun ExpenseAttachment.toAttachmentItem(): ExpenseAttachmentItem =
    ExpenseAttachmentItem(
        id = id,
        url = url,
        type = type,
        createdAt = createdAt.toIsoString(),
    )

private fun DomainUserSummary.toDto(): ExpenseUserSummary =
    ExpenseUserSummary(
        id = id,
        fullName = fullName,
        email = email,
        phone = phone,
    )

private fun Instant.toIsoString(): String = toString()


package org.igorv8836.bdui.backend.mapper

import org.igorv8836.bdui.backend.core.BackendError
import org.igorv8836.bdui.backend.core.BackendResult
import org.igorv8836.bdui.backend.core.ExecutionContext
import org.igorv8836.bdui.backend.core.ValidationIssue
import org.igorv8836.bdui.contract.RemoteScreen

/**
 * Pure mapper from arbitrary input [I] to a backend-driven screen.
 * No I/O or side effects allowed inside implementations.
 */
fun interface ScreenMapper<I> {
    fun map(input: I, context: ExecutionContext): BackendResult<RemoteScreen>
}

fun <I> ScreenMapper<I>.map(input: I): BackendResult<RemoteScreen> = map(input, ExecutionContext())

/**
 * Helper to produce a validation failure with issues.
 */
fun validationError(message: String, issues: List<ValidationIssue>): BackendResult<Nothing> =
    BackendResult.failure(
        BackendError.Validation(
            message = message,
            issues = issues,
        ),
    )

/**
 * Helper to wrap an exception into mapping error.
 */
fun mappingError(message: String, throwable: Throwable? = null): BackendResult<Nothing> =
    BackendResult.failure(
        BackendError.Mapping(
            message = message,
            cause = throwable?.message,
        ),
    )

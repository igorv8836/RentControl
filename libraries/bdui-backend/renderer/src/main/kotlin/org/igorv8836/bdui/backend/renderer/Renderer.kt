package org.igorv8836.bdui.backend.renderer

import org.igorv8836.bdui.backend.core.BackendError
import org.igorv8836.bdui.backend.core.BackendResult
import org.igorv8836.bdui.backend.core.LimitConfig
import org.igorv8836.bdui.contract.RemoteScreen

/**
 * Validate and prepare screen before serialization.
 * For now we return the screen as-is after validation; serialization can be applied by the caller.
 */
fun render(
    remoteScreen: RemoteScreen,
    limits: LimitConfig = LimitConfig(),
): BackendResult<RemoteScreen> {
    val issues = validateScreen(remoteScreen, limits)
    return if (issues.isEmpty()) {
        BackendResult.success(remoteScreen)
    } else {
        BackendResult.failure(
            BackendError.Validation(
                message = "Screen validation failed",
                issues = issues,
            ),
        )
    }
}

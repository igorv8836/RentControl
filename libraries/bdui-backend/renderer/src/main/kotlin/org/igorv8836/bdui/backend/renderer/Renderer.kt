package org.igorv8836.bdui.backend.renderer

import org.igorv8836.bdui.backend.core.BackendError
import org.igorv8836.bdui.backend.core.BackendResult
import org.igorv8836.bdui.backend.core.LimitConfig
import org.igorv8836.bdui.contract.Screen

/**
 * Validate and prepare screen before serialization.
 * For now we return the screen as-is after validation; serialization can be applied by the caller.
 */
fun render(
    screen: Screen,
    limits: LimitConfig = LimitConfig(),
): BackendResult<Screen> {
    val issues = validateScreen(screen, limits)
    return if (issues.isEmpty()) {
        BackendResult.success(screen)
    } else {
        BackendResult.failure(
            BackendError.Validation(
                message = "Screen validation failed",
                issues = issues,
            ),
        )
    }
}

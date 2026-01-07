package org.igorv8836.bdui.backend.mapper

import org.igorv8836.bdui.backend.core.BackendError
import org.igorv8836.bdui.backend.core.BackendResult
import org.igorv8836.bdui.backend.core.ExecutionContext
import org.igorv8836.bdui.backend.core.ValidationIssue
import org.igorv8836.bdui.contract.ExecutionContext as UiExecutionContext
import org.igorv8836.bdui.contract.ScreenLifecycle
import org.igorv8836.bdui.contract.ScreenSettings
import org.igorv8836.bdui.contract.SectionScroll
import org.igorv8836.bdui.contract.SectionSticky
import org.igorv8836.bdui.contract.Scaffold
import org.igorv8836.bdui.contract.Theme
import org.igorv8836.bdui.contract.Trigger
import org.igorv8836.bdui.contract.Action
import org.igorv8836.bdui.contract.Condition

/**
 * Identifier for a section; usually implemented as an object.
 */
interface SectionKey {
    val id: String
}

open class SimpleSectionKey(
    override val id: String,
) : SectionKey

fun sectionKey(id: String): SectionKey = object : SectionKey {
    override val id: String = id
}

/**
 * Blueprint describing how to build a screen section via fetch -> map -> render pipeline.
 */
data class SectionBlueprint(
    val key: SectionKey,
    val sticky: SectionSticky = SectionSticky.None,
    val scroll: SectionScroll = SectionScroll(),
    val visibleIf: Condition? = null,
) {
    val id: String = key.id
}

/**
 * Blueprint for an entire screen before components are rendered.
 */
data class BackendScreenDraft(
    val id: String,
    val version: Int,
    val sections: List<SectionBlueprint>,
    val scaffold: Scaffold? = null,
    val actions: List<Action> = emptyList(),
    val triggers: List<Trigger> = emptyList(),
    val theme: Theme? = null,
    val settings: ScreenSettings = ScreenSettings(),
    val lifecycle: ScreenLifecycle = ScreenLifecycle(),
    val context: UiExecutionContext = UiExecutionContext(),
)

/**
 * Mapper from input to [BackendScreenDraft]. Fetch/render happen later in the engine.
 */
fun interface ScreenDraftMapper<I> {
    suspend fun map(input: I, context: ExecutionContext): BackendResult<BackendScreenDraft>
}

suspend fun <I> ScreenDraftMapper<I>.map(input: I): BackendResult<BackendScreenDraft> = map(input, ExecutionContext())

/**
 * Helper to produce a validation failure with issues.
 */
fun specValidationError(message: String, issues: List<ValidationIssue>): BackendResult<Nothing> =
    BackendResult.failure(
        BackendError.Validation(
            message = message,
            issues = issues,
        ),
    )

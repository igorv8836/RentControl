package org.igorv8836.bdui.backend.runtime

import org.igorv8836.bdui.backend.core.BackendError
import org.igorv8836.bdui.backend.core.BackendResult
import org.igorv8836.bdui.backend.core.ExecutionContext
import org.igorv8836.bdui.backend.mapper.SectionBlueprint
import org.igorv8836.bdui.contract.ComponentNode

fun interface SectionFetcher {
    suspend fun fetch(input: Any?, context: ExecutionContext): BackendResult<Any?>
}

fun interface SectionMapper {
    suspend fun map(data: Any?, context: ExecutionContext): BackendResult<Any?>
}

fun interface SectionRenderer<M : Any?> {
    fun render(model: M, blueprint: SectionBlueprint): ComponentNode
}

internal object DefaultSectionFetcher : SectionFetcher {
    override suspend fun fetch(input: Any?, context: ExecutionContext): BackendResult<Any?> =
        BackendResult.success(null)
}

internal object DefaultSectionMapper : SectionMapper {
    override suspend fun map(data: Any?, context: ExecutionContext): BackendResult<Any?> =
        BackendResult.success(data)
}

internal fun missingRendererError(sectionId: String, modelClass: String): BackendResult<Nothing> =
    BackendResult.failure(
        BackendError.Mapping(
            message = "No renderer registered for sectionId=$sectionId modelClass=$modelClass",
        ),
    )

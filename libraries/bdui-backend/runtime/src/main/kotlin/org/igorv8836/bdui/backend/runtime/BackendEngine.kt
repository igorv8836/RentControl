package org.igorv8836.bdui.backend.runtime

import org.igorv8836.bdui.backend.core.BackendError
import org.igorv8836.bdui.backend.core.BackendResult
import org.igorv8836.bdui.backend.core.ExecutionContext
import org.igorv8836.bdui.backend.core.LimitConfig
import org.igorv8836.bdui.backend.renderer.render
import org.igorv8836.bdui.contract.RemoteScreen
import java.util.ServiceLoader

class BackendEngine private constructor(
    private val registry: BackendRegistry,
    private val limits: LimitConfig,
) {
    suspend fun render(screenId: String, input: Any?, context: ExecutionContext = ExecutionContext()): BackendResult<RemoteScreen> {
        val entry = registry.mappers[screenId]
            ?: return BackendResult.failure(
                BackendError.Mapping(
                    message = "No mapper registered for screenId=$screenId",
                ),
            )
        return entry.map(input, context).flatMap { screen ->
            registry.validators.fold(BackendResult.success(screen)) { acc, validator ->
                acc.flatMap { current ->
                    val issues = validator(current)
                    if (issues.isEmpty()) BackendResult.success(current)
                    else BackendResult.failure(
                        BackendError.Validation(
                            message = "Validation failed",
                            issues = issues,
                        ),
                    )
                }
            }.flatMap { validated ->
                render(validated, limits)
            }
        }
    }

    companion object {
        fun create(
            limits: LimitConfig = LimitConfig(),
            autoLoadModules: Boolean = true,
            modules: List<BackendModule> = emptyList(),
        ): BackendEngine {
            val builder = BackendRegistryBuilder()
            if (autoLoadModules) {
                ServiceLoader.load(BackendModule::class.java).forEach { module ->
                    module.register(builder)
                }
            }
            modules.forEach { it.register(builder) }
            return BackendEngine(builder.build(), limits)
        }
    }
}

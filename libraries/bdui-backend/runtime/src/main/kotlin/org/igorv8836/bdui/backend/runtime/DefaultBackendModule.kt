package org.igorv8836.bdui.backend.runtime

import org.igorv8836.bdui.backend.core.LimitConfig
import org.igorv8836.bdui.backend.renderer.validateScreen

/**
 * Default module that registers base validator.
 */
class DefaultBackendModule(
    private val limits: LimitConfig = LimitConfig(),
) : BackendModule {
    override fun register(registry: BackendRegistryBuilder) {
        registry.validator { screen -> validateScreen(screen, limits) }
    }
}

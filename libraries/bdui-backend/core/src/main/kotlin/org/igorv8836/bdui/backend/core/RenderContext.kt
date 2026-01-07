package org.igorv8836.bdui.backend.core

import org.igorv8836.bdui.contract.Action

/**
 * Контекст рендера для сборки экшенов из DSL.
 */
class RenderContext {
    private val collected = mutableListOf<Action>()
    val actions: List<Action> get() = collected

    fun register(action: Action) {
        collected += action
    }
}

package org.igorv8836.bdui.backend.runtime.engine

import org.igorv8836.bdui.backend.core.RenderContext
import org.igorv8836.bdui.contract.ComponentNode
import org.igorv8836.bdui.contract.Scaffold

/**
 * Маркер модели, которую отдает mapper для рендера.
 */
interface RenderingData

interface Renderer<R : RenderingData, O> {
    fun render(data: R, ctx: RenderContext): O
}

typealias SectionRenderer<R> = Renderer<R, ComponentNode>
typealias ScaffoldRenderer<R> = Renderer<R, Scaffold>

package org.igorv8836.bdui.backend.dsl

import org.igorv8836.bdui.contract.ComponentNode

/**
 * Render hook that produces the content for a section.
 * Keeps the ScreenBuilder declarative while letting each section
 * live in its own class or factory.
 */
fun interface SectionRenderer {
    fun render(): ComponentNode
}

fun sectionRenderer(block: () -> ComponentNode): SectionRenderer = SectionRenderer { block() }

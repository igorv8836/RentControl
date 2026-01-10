package org.igorv8836.bdui.backend.dsl

import org.igorv8836.bdui.contract.ComponentNode
import org.igorv8836.bdui.contract.Condition
import org.igorv8836.bdui.contract.Section
import org.igorv8836.bdui.contract.SectionScroll
import org.igorv8836.bdui.contract.Sticky
import org.igorv8836.bdui.backend.mapper.SectionBlueprint
import org.igorv8836.bdui.backend.mapper.SectionKey

class SectionsBuilder {
    private val sections = mutableListOf<SectionDraft>()

    fun section(
        key: SectionKey,
        sticky: Sticky? = null,
        scroll: SectionScroll = SectionScroll(),
        visibleIf: Condition? = null,
        renderer: SectionRenderer,
    ) {
        sections += SectionDraft(
            id = key.id,
            sticky = sticky,
            scroll = scroll,
            visibleIf = visibleIf,
            renderer = renderer,
            key = key,
        )
    }

    fun section(
        key: SectionKey,
        sticky: Sticky? = null,
        scroll: SectionScroll = SectionScroll(),
        visibleIf: Condition? = null,
        content: ComponentNode,
    ) {
        section(
            key = key,
            sticky = sticky,
            scroll = scroll,
            visibleIf = visibleIf,
            renderer = sectionRenderer { content },
        )
    }

    fun build(): List<SectionDraft> = sections.toList()
}

fun section(
    key: SectionKey,
    sticky: Sticky? = null,
    scroll: SectionScroll = SectionScroll(),
    visibleIf: Condition? = null,
    content: ComponentNode,
): Section = Section(
    id = key.id,
    content = content,
    sticky = sticky,
    scroll = scroll,
    visibleIf = visibleIf,
)

data class SectionDraft(
    val id: String,
    val sticky: Sticky?,
    val scroll: SectionScroll,
    val visibleIf: Condition?,
    val renderer: SectionRenderer,
    val key: SectionKey,
)

class SectionsDraftBuilder {
    private val sections = mutableListOf<SectionBlueprint>()

    fun section(
        key: SectionKey,
        sticky: Sticky? = null,
        scroll: SectionScroll = SectionScroll(),
        visibleIf: Condition? = null,
    ) {
        sections += SectionBlueprint(
            key = key,
            sticky = sticky,
            scroll = scroll,
            visibleIf = visibleIf,
        )
    }

    fun build(): List<SectionBlueprint> = sections.toList()
}

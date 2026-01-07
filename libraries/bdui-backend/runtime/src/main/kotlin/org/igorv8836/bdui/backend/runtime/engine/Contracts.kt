package org.igorv8836.bdui.backend.runtime.engine

import org.igorv8836.bdui.contract.Action
import org.igorv8836.bdui.contract.Condition
import org.igorv8836.bdui.contract.ScreenSettings
import org.igorv8836.bdui.contract.SectionScroll
import org.igorv8836.bdui.contract.SectionSticky
import org.igorv8836.bdui.contract.Trigger

/**
 * Маркер входных параметров экрана.
 */
interface Parameters

/**
 * Маркер для всех драфтов.
 */
interface Draft

data class SectionDraft(
    val key: SectionKey,
    val sticky: SectionSticky = SectionSticky.None,
    val scroll: SectionScroll = SectionScroll(),
    val visibleIf: Condition? = null,
) : Draft

data class ScaffoldDraft(
    val key: SectionKey,
    val visibleIf: Condition? = null,
) : Draft

data class ScreenDraft(
    val sections: List<SectionDraft>,
    val scaffold: ScaffoldDraft? = null,
    val settings: ScreenSettings = ScreenSettings(),
    val actions: List<Action> = emptyList(),
    val triggers: List<Trigger> = emptyList(),
)

/**
 * Ключ секции/скаффолда. Обычно object.
 */
interface SectionKey {
    val id: String
}

open class SimpleSectionKey(
    override val id: String,
) : SectionKey

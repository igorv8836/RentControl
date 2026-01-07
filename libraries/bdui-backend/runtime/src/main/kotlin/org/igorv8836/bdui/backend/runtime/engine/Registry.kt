package org.igorv8836.bdui.backend.runtime.engine

import kotlin.reflect.KClass

class BackendRegistry {
    val screenBuilders: MutableMap<KClass<out Parameters>, ScreenBuilder<out Parameters>> = mutableMapOf()
    val draftMappers: MutableMap<SectionKey, DraftMapper<out Draft, out RenderingData>> = mutableMapOf()
    val draftRenderModels: MutableMap<SectionKey, KClass<out RenderingData>> = mutableMapOf()
    val scaffoldMappers: MutableMap<SectionKey, DraftMapper<out Draft, out RenderingData>> = mutableMapOf()
    val scaffoldRenderModels: MutableMap<SectionKey, KClass<out RenderingData>> = mutableMapOf()
    val renderers: MutableMap<KClass<out RenderingData>, Renderer<out RenderingData, out Any>> = mutableMapOf()
}

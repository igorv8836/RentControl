package org.igorv8836.bdui.backend.runtime.annotations

import org.igorv8836.bdui.backend.runtime.engine.Draft
import org.igorv8836.bdui.backend.runtime.engine.DraftMapper
import org.igorv8836.bdui.backend.runtime.engine.Parameters
import org.igorv8836.bdui.backend.runtime.engine.Renderer
import org.igorv8836.bdui.backend.runtime.engine.RenderingData
import org.igorv8836.bdui.backend.runtime.engine.ScreenBuilder
import org.igorv8836.bdui.backend.runtime.engine.SectionKey
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ScreenBinding(
    val params: KClass<out Parameters>,
    val builder: KClass<out ScreenBuilder<out Parameters>>,
    val scanPackages: Array<String> = [],
)

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class DraftBinding(
    val key: KClass<out SectionKey>,
    val mapper: KClass<out DraftMapper<out Draft, out RenderingData>>,
    val renderModel: KClass<out RenderingData>,
    val renderer: KClass<out Renderer<out RenderingData, out Any>>,
)

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ScaffoldBinding(
    val key: KClass<out SectionKey>,
    val mapper: KClass<out DraftMapper<out Draft, out RenderingData>>,
    val renderModel: KClass<out RenderingData>,
    val renderer: KClass<out Renderer<out RenderingData, out Any>>,
)

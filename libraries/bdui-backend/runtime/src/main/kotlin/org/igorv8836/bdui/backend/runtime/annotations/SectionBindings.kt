package org.igorv8836.bdui.backend.runtime.annotations

import kotlin.reflect.KClass
import org.igorv8836.bdui.backend.mapper.SectionKey

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class SectionFetcherBinding(val key: KClass<out SectionKey>)

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class SectionMapperBinding(
    val key: KClass<out SectionKey>,
    val model: KClass<out Any>,
)

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class SectionRendererBinding(
    val model: KClass<out Any>,
)

package org.igorv8836.bdui.backend.runtime.engine

import org.igorv8836.bdui.backend.runtime.annotations.DraftBinding
import org.igorv8836.bdui.backend.runtime.annotations.ScaffoldBinding
import org.igorv8836.bdui.backend.runtime.annotations.ScreenBinding
import org.reflections.Reflections
import kotlin.reflect.KClass

object AnnotationRegistrar {
    fun registerAll(
        registry: BackendRegistry,
        packages: List<String>,
        classLoader: ClassLoader = Thread.currentThread().contextClassLoader,
    ) {
        if (packages.isEmpty()) return
        registerAllInternal(registry, packages.toSet(), mutableSetOf(), classLoader)
    }

    private fun registerAllInternal(
        registry: BackendRegistry,
        packages: Set<String>,
        visited: MutableSet<String>,
        classLoader: ClassLoader,
    ) {
        val newPkgs = packages.filter { visited.add(it) }
        if (newPkgs.isEmpty()) return
        val reflections = Reflections(*newPkgs.toTypedArray(), classLoader)

        reflections.getTypesAnnotatedWith(ScreenBinding::class.java).forEach { clazz ->
            val ann = clazz.getAnnotation(ScreenBinding::class.java)
            val builder = instantiate(ann.builder.java) as? ScreenBuilder<out Parameters> ?: return@forEach
            registry.screenBuilders[ann.params] = builder
            if (ann.scanPackages.isNotEmpty()) {
                registerAllInternal(registry, ann.scanPackages.toSet(), visited, classLoader)
            }
        }

        reflections.getTypesAnnotatedWith(DraftBinding::class.java).forEach { clazz ->
            val ann = clazz.getAnnotation(DraftBinding::class.java)
            val key = instantiateKey(ann.key)
            val mapper = instantiate(ann.mapper.java) as? DraftMapper<out Draft, out RenderingData> ?: return@forEach
            registry.draftMappers[key] = mapper
            registry.draftRenderModels[key] = ann.renderModel
            val renderer = instantiate(ann.renderer.java) as? Renderer<out RenderingData, out Any> ?: return@forEach
            registry.renderers[ann.renderModel] = renderer
        }

        reflections.getTypesAnnotatedWith(ScaffoldBinding::class.java).forEach { clazz ->
            val ann = clazz.getAnnotation(ScaffoldBinding::class.java)
            val key = instantiateKey(ann.key)
            val mapper = instantiate(ann.mapper.java) as? DraftMapper<out Draft, out RenderingData> ?: return@forEach
            registry.scaffoldMappers[key] = mapper
            registry.scaffoldRenderModels[key] = ann.renderModel
            val renderer = instantiate(ann.renderer.java) as? Renderer<out RenderingData, out Any> ?: return@forEach
            registry.renderers[ann.renderModel] = renderer
        }
    }

    private fun instantiateKey(kClass: KClass<out SectionKey>): SectionKey {
        kClass.java.declaredFields.firstOrNull { it.name == "INSTANCE" }?.let {
            it.isAccessible = true
            (it.get(null) as? SectionKey)?.let { key -> return key }
        }
        return kClass.java.getDeclaredConstructor().apply { isAccessible = true }.newInstance()
    }

    private fun <T : Any> instantiate(clazz: Class<out T>): Any? {
        clazz.declaredFields.firstOrNull { it.name == "INSTANCE" }?.let {
            it.isAccessible = true
            return it.get(null)
        }
        return try {
            clazz.getDeclaredConstructor().apply { isAccessible = true }.newInstance()
        } catch (_: Exception) {
            null
        }
    }
}

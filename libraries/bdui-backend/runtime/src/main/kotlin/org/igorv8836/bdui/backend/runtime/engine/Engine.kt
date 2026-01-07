package org.igorv8836.bdui.backend.runtime.engine

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.igorv8836.bdui.backend.core.BackendError
import org.igorv8836.bdui.backend.core.BackendResult
import org.igorv8836.bdui.backend.core.RenderContext
import org.igorv8836.bdui.contract.Action
import org.igorv8836.bdui.contract.ComponentNode
import org.igorv8836.bdui.contract.Layout
import org.igorv8836.bdui.contract.RemoteScreen
import org.igorv8836.bdui.contract.Scaffold as UiScaffold
import org.igorv8836.bdui.contract.Section as UiSection

class Engine(private val registry: BackendRegistry) {
    suspend fun render(params: Parameters): BackendResult<RemoteScreen> {
        val builder = registry.screenBuilders[params::class] as? ScreenBuilder<Parameters>
            ?: return BackendResult.failure(
                BackendError.Mapping(message = "No ScreenBuilder registered for params=${params::class.simpleName}"),
            )
        val draft = builder.build(params)

        val fetcherCtx = FetcherContext()

        val sectionResults = renderSections(draft, params, fetcherCtx)
        val sectionFailure = sectionResults.firstOrNull { it is BackendResult.Failure } as? BackendResult.Failure
        if (sectionFailure != null) return sectionFailure

        val sections = sectionResults.filterIsInstance<BackendResult.Success<Pair<UiSection, List<Action>>>>()
            .map { it.value.first }
        val actionsFromSections = sectionResults.filterIsInstance<BackendResult.Success<Pair<UiSection, List<Action>>>>()
            .flatMap { it.value.second }

        val scaffoldResult = draft.scaffold?.let { renderScaffold(it, params, fetcherCtx) }
        val scaffoldUi = when (scaffoldResult) {
            is BackendResult.Failure -> return scaffoldResult
            is BackendResult.Success -> scaffoldResult.value.first
            null -> null
        }
        val actionsFromScaffold = when (scaffoldResult) {
            is BackendResult.Success -> scaffoldResult.value.second
            else -> emptyList()
        }

        val screen = RemoteScreen(
            id = params::class.simpleName ?: "screen",
            version = 1,
            layout = Layout(
                root = sections.firstOrNull()?.content,
                sections = sections,
                scaffold = scaffoldUi,
            ),
            actions = draft.actions + actionsFromSections + actionsFromScaffold,
            triggers = draft.triggers,
            settings = draft.settings,
        )
        return BackendResult.success(screen)
    }

    private suspend fun renderSections(
        draft: ScreenDraft,
        params: Parameters,
        fetcherCtx: FetcherContext,
    ): List<BackendResult<Pair<UiSection, List<Action>>>> = coroutineScope {
        draft.sections.map { section ->
            async { renderSection(section, params, fetcherCtx) }
        }.awaitAll()
    }

    private suspend fun renderSection(
        section: SectionDraft,
        params: Parameters,
        fetcherCtx: FetcherContext,
    ): BackendResult<Pair<UiSection, List<Action>>> {
        val mapper = registry.draftMappers[section.key] as? DraftMapper<Draft, RenderingData>
            ?: return BackendResult.failure(
                BackendError.Mapping(message = "No mapper for section ${section.key.id}"),
            )
        val modelClass = registry.draftRenderModels[section.key]
            ?: return BackendResult.failure(
                BackendError.Mapping(message = "No render model for section ${section.key.id}"),
            )
        val renderer = registry.renderers[modelClass] as? Renderer<RenderingData, Any>
            ?: return BackendResult.failure(
                BackendError.Mapping(message = "No renderer for section ${section.key.id}"),
            )
        val res = mapper.map(section, params, fetcherCtx)
        return res.flatMap { data ->
            val renderContext = RenderContext()
            val ui = renderer.render(data, renderContext) as? ComponentNode
                ?: return@flatMap BackendResult.failure(
                    BackendError.Mapping(message = "Renderer returned non-component for section ${section.key.id}"),
                )
            BackendResult.success(
                UiSection(
                    id = section.key.id,
                    content = ui,
                    sticky = section.sticky,
                    scroll = section.scroll,
                    visibleIf = section.visibleIf,
                ),
            ).map { it to renderContext.actions }
        }
    }

    private suspend fun renderScaffold(
        scaffold: ScaffoldDraft,
        params: Parameters,
        fetcherCtx: FetcherContext,
    ): BackendResult<Pair<UiScaffold, List<Action>>> {
        val mapper = registry.scaffoldMappers[scaffold.key] as? DraftMapper<Draft, RenderingData>
            ?: return BackendResult.failure(
                BackendError.Mapping(message = "No mapper for scaffold ${scaffold.key.id}"),
            )
        val modelClass = registry.scaffoldRenderModels[scaffold.key]
            ?: return BackendResult.failure(
                BackendError.Mapping(message = "No render model for scaffold ${scaffold.key.id}"),
            )
        val renderer = registry.renderers[modelClass] as? Renderer<RenderingData, Any>
            ?: return BackendResult.failure(
                BackendError.Mapping(message = "No renderer for scaffold ${scaffold.key.id}"),
            )
        val res = mapper.map(scaffold as Draft, params, fetcherCtx)
        return res.flatMap { data ->
            val ctx = RenderContext()
            val ui = renderer.render(data, ctx) as? UiScaffold
                ?: return@flatMap BackendResult.failure(
                    BackendError.Mapping(message = "Renderer returned non-scaffold for ${scaffold.key.id}"),
                )
            BackendResult.success(ui to ctx.actions)
        }
    }
}

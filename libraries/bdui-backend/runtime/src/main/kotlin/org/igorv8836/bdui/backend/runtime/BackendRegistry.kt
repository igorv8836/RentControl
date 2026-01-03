package org.igorv8836.bdui.backend.runtime

import org.igorv8836.bdui.backend.core.BackendError
import org.igorv8836.bdui.backend.core.BackendResult
import org.igorv8836.bdui.backend.core.ExecutionContext
import org.igorv8836.bdui.backend.core.ValidationIssue
import org.igorv8836.bdui.backend.data.DataProvider
import org.igorv8836.bdui.backend.mapper.ScreenMapper
import org.igorv8836.bdui.contract.RemoteScreen

typealias ScreenValidator = (RemoteScreen) -> List<ValidationIssue>

class BackendRegistryBuilder {
    private val mappers = MutableMapperRegistry()
    private val validators = mutableListOf<ScreenValidator>()
    private val dataProviders = MutableDataProviderRegistry()

    fun <I> mapper(
        screenId: String,
        mapper: ScreenMapper<I>,
        caster: (Any?) -> I? = { it as? I },
    ) {
        mappers.register(screenId, mapper, caster)
    }

    fun validator(validator: ScreenValidator) {
        validators += validator
    }

    fun <Req, Res> dataProvider(name: String, provider: DataProvider<Req, Res>) {
        dataProviders.register(name, provider as DataProvider<Any?, Any?>)
    }

    fun build(): BackendRegistry = BackendRegistry(
        mappers = mappers.build(),
        validators = validators.toList(),
        dataProviders = dataProviders.build(),
    )
}

data class BackendRegistry(
    val mappers: Map<String, MapperEntry>,
    val validators: List<ScreenValidator>,
    val dataProviders: Map<String, DataProvider<Any?, Any?>>,
)

internal class MutableMapperRegistry {
    private val entries = mutableMapOf<String, MapperEntry>()

    fun <I> register(
        screenId: String,
        mapper: ScreenMapper<I>,
        caster: (Any?) -> I?,
    ) {
        entries[screenId] = MapperEntry { raw, context ->
            val typed = caster(raw)
            if (typed == null) {
                BackendResult.failure(
                    BackendError.Mapping(
                        message = "Invalid input type for mapper: $screenId",
                        cause = raw?.let { it::class.simpleName },
                    ),
                )
            } else {
                mapper.map(typed, context)
            }
        }
    }

    fun build(): Map<String, MapperEntry> = entries.toMap()
}

fun interface MapperEntry {
    suspend fun map(input: Any?, context: ExecutionContext): BackendResult<RemoteScreen>
}

internal class MutableDataProviderRegistry {
    private val providers = mutableMapOf<String, DataProvider<Any?, Any?>>()

    fun register(name: String, provider: DataProvider<Any?, Any?>) {
        providers[name] = provider
    }

    fun build(): Map<String, DataProvider<Any?, Any?>> = providers.toMap()
}

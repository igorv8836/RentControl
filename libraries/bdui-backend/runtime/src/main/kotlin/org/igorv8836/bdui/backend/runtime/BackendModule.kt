package org.igorv8836.bdui.backend.runtime

/**
 * Module entrypoint used by ServiceLoader to register mappers/providers/validators.
 */
interface BackendModule {
    fun register(registry: BackendRegistryBuilder)
}

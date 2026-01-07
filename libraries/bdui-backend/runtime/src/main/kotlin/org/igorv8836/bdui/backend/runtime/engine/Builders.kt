package org.igorv8836.bdui.backend.runtime.engine

interface ScreenBuilder<P : Parameters> {
    fun build(params: P): ScreenDraft
}

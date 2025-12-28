package org.igorv8836.bdui.runtime.controller

import kotlinx.coroutines.flow.StateFlow
import org.igorv8836.bdui.core.variables.VariableStore
import org.igorv8836.bdui.runtime.ScreenState

interface ScreenController {
    val state: StateFlow<ScreenState>
    val variableStore: VariableStore
    fun onOpen()
    fun onAppear()
    fun onFullyVisible()
    fun onDisappear()
    fun dispose()
}
package org.igorv8836.bdui.backend.dsl

import org.igorv8836.bdui.backend.core.RenderContext
import org.igorv8836.bdui.contract.Action
import org.igorv8836.bdui.contract.Color
import org.igorv8836.bdui.contract.ForwardAction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ButtonColorDslTest {

    @Test
    fun `button keeps text and background colors and registers action`() {
        val ctx = RenderContext()
        val scope = ContainerScope(ctx)
        val action: Action = ForwardAction(id = "go", path = "catalog")

        val button = scope.button(
            id = "cta",
            title = "Open",
            action = action,
            textColor = Color(light = "#111111", dark = "#EEEEEE"),
            backgroundColor = Color(light = "#222222"),
        )

        assertEquals("#111111", button.textColor?.light)
        assertEquals("#EEEEEE", button.textColor?.dark)
        assertEquals("#222222", button.backgroundColor?.light)
        // ensure action was auto registered in context
        assertTrue(ctx.actions.any { it.id == action.id })
    }
}

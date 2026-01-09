package org.igorv8836.bdui.backend.dsl

import org.igorv8836.bdui.contract.Color
import org.igorv8836.bdui.contract.ContainerDirection
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ColorDslTest {

    @Test
    fun `container accepts background color`() {
        val scope = ContainerScope()
        val hex = "#112233"
        val dark = "#AABBCC"

        val container = scope.container(
            id = "root",
            direction = ContainerDirection.Column,
            backgroundColor = color(hex, dark),
        )

        assertNotNull(container.backgroundColor)
        assertEquals(hex, container.backgroundColor?.light)
        assertEquals(dark, container.backgroundColor?.dark)
    }

    @Test
    fun `color helper builds Color`() {
        val c = color("#AABBCC", "#010203")
        assertEquals(Color("#AABBCC", "#010203"), c)
    }
}

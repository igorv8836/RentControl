package org.igorv8836.bdui.renderer.node

import androidx.compose.ui.graphics.Color
import org.igorv8836.bdui.contract.Color as ContractColor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ColorParserTest {

    @Test
    fun `parses rgb hex`() {
        val result = parseColor(ContractColor("#112233"), isDark = false)
        assertEquals(Color(0xFF112233), result)
    }

    @Test
    fun `parses argb hex`() {
        val result = parseColor(ContractColor("#AA112233"), isDark = false)
        assertEquals(Color(0xAA112233), result)
    }

    @Test
    fun `prefers dark hex in dark mode`() {
        val result = parseColor(ContractColor(light = "#112233", dark = "#445566"), isDark = true)
        assertEquals(Color(0xFF445566), result)
    }

    @Test
    fun `returns null on invalid`() {
        assertNull(parseColor(null, isDark = false))
        assertNull(parseColor(ContractColor("#FFF"), isDark = false))
        assertNull(parseColor(ContractColor("not-a-color"), isDark = false))
    }
}

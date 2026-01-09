package org.igorv8836.bdui.contract

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ColorUsageSerializationTest {

    private val json = Json { ignoreUnknownKeys = false }

    @Test
    fun `button element serializes text and background colors`() {
        val element = ButtonElement(
            id = "cta",
            title = "Open",
            actionId = "go",
            textColor = Color(light = "#111111", dark = "#EEEEEE"),
            backgroundColor = Color(light = "#222222"),
        )

        val encoded = json.encodeToString(element)
        val decoded = json.decodeFromString<ButtonElement>(encoded)

        assertEquals("#111111", decoded.textColor?.light)
        assertEquals("#EEEEEE", decoded.textColor?.dark)
        assertEquals("#222222", decoded.backgroundColor?.light)
        assertNotNull(decoded)
    }
}

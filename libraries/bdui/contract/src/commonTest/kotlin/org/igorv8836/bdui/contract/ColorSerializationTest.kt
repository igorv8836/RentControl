package org.igorv8836.bdui.contract

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.experimental.ExperimentalNativeApi

@OptIn(ExperimentalNativeApi::class)
class ColorSerializationTest {

    private val json = Json { encodeDefaults = false }

    @Test
    fun `color serializes hex field`() {
        val payload = Color(light = "#112233", dark = "#445566")
        val encoded = json.encodeToString(payload)
        assertEquals("""{"light":"#112233","dark":"#445566"}""", encoded)
    }

    @Test
    fun `component carries color`() {
        val text = TextElement(
            id = "t1",
            text = "Hello",
            textColor = Color("#010203", "#0A0B0C"),
        )
        val encoded = json.encodeToString(text)
        assert(encoded.contains("\"textColor\":{\"light\":\"#010203\",\"dark\":\"#0A0B0C\"}"))
    }
}

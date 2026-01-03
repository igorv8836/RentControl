package org.igorv8836.bdui.actions.navigation

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.igorv8836.bdui.actions.serialization.ActionSerializers
import org.igorv8836.bdui.contract.ActionResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RemoteActionSerializationTest {

    private val json = Json {
        classDiscriminator = "type"
        ignoreUnknownKeys = true
        serializersModule = ActionSerializers.module
    }

    @Test
    fun roundTripRemoteActionInResponse() {
        val response = ActionResponse(
            actions = listOf(
                RemoteAction(id = "remote-login", path = "/auth/login", parameters = mapOf("k" to "v")),
                ForwardAction(id = "go-home", path = "/home"),
            ),
        )

        val encoded = json.encodeToString(response)
        val decoded = json.decodeFromString<ActionResponse>(encoded)

        assertEquals(2, decoded.actions.size)
        val remote = decoded.actions.first() as RemoteAction
        assertEquals("remote-login", remote.id)
        assertEquals("/auth/login", remote.path)
        assertTrue(remote.parameters.containsKey("k"))
        val forward = decoded.actions[1] as ForwardAction
        assertEquals("go-home", forward.id)
    }
}

package org.igorv8836.bdui.contract

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface Action {
    val id: String
}

@Serializable
@SerialName("RemoteAction")
data class RemoteAction(
    override val id: String,
    val path: String,
    val parameters: Map<String, String> = emptyMap(),
) : Action

@Serializable
data class ActionResponse(
    val actions: List<Action> = emptyList(),
)

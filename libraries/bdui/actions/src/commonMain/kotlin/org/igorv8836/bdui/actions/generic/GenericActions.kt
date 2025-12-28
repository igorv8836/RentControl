package org.igorv8836.bdui.actions.generic

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.igorv8836.bdui.contract.Action
import org.igorv8836.bdui.contract.Analytics
import org.igorv8836.bdui.core.actions.ActionHandler
import org.igorv8836.bdui.core.context.ActionContext

@Serializable
@SerialName("SubmitAction")
data class SubmitAction(
    override val id: String,
    val payload: Map<String, String> = emptyMap(),
) : Action

class SubmitActionHandler : ActionHandler<SubmitAction> {
    override suspend fun handle(action: SubmitAction, context: ActionContext) {
        // Default handler intentionally left blank; provide a custom handler if needed.
    }
}

@Serializable
@SerialName("AnalyticsAction")
data class AnalyticsAction(
    override val id: String,
    val analytics: Analytics,
) : Action

class AnalyticsActionHandler : ActionHandler<AnalyticsAction> {
    override suspend fun handle(action: AnalyticsAction, context: ActionContext) {
        // Default handler intentionally left blank; provide a custom handler if needed.
    }
}

@Serializable
@SerialName("CustomAction")
data class CustomAction(
    override val id: String,
    val name: String,
    val parameters: Map<String, String> = emptyMap(),
) : Action

class CustomActionHandler : ActionHandler<CustomAction> {
    override suspend fun handle(action: CustomAction, context: ActionContext) {
        // Default handler intentionally left blank; provide a custom handler if needed.
    }
}

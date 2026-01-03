package org.igorv8836.bdui.core.context

import org.igorv8836.bdui.core.navigation.Navigator
import org.igorv8836.bdui.core.actions.ActionFetcher

data class ActionContext(
    val navigator: Navigator,
    val screenContext: ScreenContext,
    val screenId: String? = null,
    val actionFetcher: ActionFetcher? = null,
)

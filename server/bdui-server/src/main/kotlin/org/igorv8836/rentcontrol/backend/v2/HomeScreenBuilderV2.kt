package org.igorv8836.rentcontrol.backend.v2

import org.igorv8836.bdui.backend.dsl.variableChangedTrigger
import org.igorv8836.bdui.backend.runtime.annotations.ScreenBinding
import org.igorv8836.bdui.backend.runtime.engine.ScreenBuilder
import org.igorv8836.bdui.backend.runtime.engine.ScreenDraft
import org.igorv8836.bdui.backend.runtime.engine.ScaffoldDraft
import org.igorv8836.bdui.backend.runtime.engine.SectionDraft
import org.igorv8836.bdui.contract.Action
import org.igorv8836.bdui.contract.Condition
import org.igorv8836.bdui.contract.NumberValue
import org.igorv8836.bdui.contract.PaginationSettings
import org.igorv8836.bdui.contract.PullToRefresh
import org.igorv8836.bdui.contract.ScreenSettings
import org.igorv8836.bdui.contract.SectionScroll
import org.igorv8836.bdui.contract.StringValue
import org.igorv8836.bdui.contract.VariableScope

@ScreenBinding(
    params = HomeParams::class,
    builder = HomeScreenBuilderV2::class,
    scanPackages = ["org.igorv8836.rentcontrol.backend.v2"],
)
class HomeScreenBuilderV2 : ScreenBuilder<HomeParams> {
    override fun build(params: HomeParams): ScreenDraft {
        val setUser = org.igorv8836.bdui.backend.dsl.setVariableAction(
            id = "set-user",
            key = "user_name",
            value = StringValue("Guest"),
            scope = VariableScope.Global,
        )
        val initVisits = org.igorv8836.bdui.backend.dsl.setVariableAction(
            id = "init-visits",
            key = "visits",
            value = NumberValue(0.0),
            scope = VariableScope.Global,
        )

        val actions = listOf<Action>(
            setUser,
            initVisits,
        )

        return ScreenDraft(
            sections = listOf(
                SectionDraft(key = HomeHeaderKey),
                SectionDraft(key = HomeOffersKey, scroll = SectionScroll(enabled = true)),
                SectionDraft(key = HomeFooterKey),
            ),
            scaffold = ScaffoldDraft(key = HomeScaffoldKey),
            settings = ScreenSettings(
                scrollable = true,
                pullToRefresh = PullToRefresh(enabled = true),
                pagination = PaginationSettings(enabled = false),
            ),
            actions = actions,
            triggers = listOf(
                variableChangedTrigger(
                    id = "welcome-trigger",
                    key = "user_name",
                    actions = emptyList(),
                    condition = Condition(
                        key = "user_name",
                        equals = StringValue("Guest"),
                    ),
                ),
            ),
        )
    }
}

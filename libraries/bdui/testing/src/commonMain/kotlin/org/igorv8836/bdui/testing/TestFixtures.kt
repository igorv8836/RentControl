package org.igorv8836.bdui.testing

import kotlinx.coroutines.delay
import org.igorv8836.bdui.contract.ButtonElement
import org.igorv8836.bdui.contract.ButtonKind
import org.igorv8836.bdui.contract.Action
import org.igorv8836.bdui.contract.ActionType
import org.igorv8836.bdui.contract.Container
import org.igorv8836.bdui.contract.Layout
import org.igorv8836.bdui.contract.Screen
import org.igorv8836.bdui.contract.TextElement
import org.igorv8836.bdui.contract.ContainerDirection
import org.igorv8836.bdui.runtime.ScreenRepository

object Fixtures {

    fun screenWithTextAndButton(): Screen {
        val action = Action(id = "continue", type = ActionType.Navigate)
        return Screen(
            id = "demo",
            version = 1,
            layout = Layout(
                root = Container(
                    id = "root",
                    direction = ContainerDirection.Column,
                    children = listOf(
                        TextElement(id = "title", textKey = "title"),
                        ButtonElement(
                            id = "cta",
                            titleKey = "cta",
                            actionId = action.id,
                            kind = ButtonKind.Primary,
                        ),
                    ),
                ),
            ),
            actions = listOf(action),
        )
    }
}

class FakeScreenRepository(
    private val screen: Screen = Fixtures.screenWithTextAndButton(),
    private val delayMs: Long = 0,
) : ScreenRepository {

    override suspend fun fetch(screenId: String, params: Map<String, String>): Result<Screen> {
        if (delayMs > 0) {
            delay(delayMs)
        }
        return Result.success(screen.copy(id = screenId))
    }
}

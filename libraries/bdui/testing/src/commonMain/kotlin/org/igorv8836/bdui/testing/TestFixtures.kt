package org.igorv8836.bdui.testing

import kotlinx.coroutines.delay
import org.igorv8836.bdui.contract.ButtonElement
import org.igorv8836.bdui.contract.ButtonKind
import org.igorv8836.bdui.contract.ForwardAction
import org.igorv8836.bdui.contract.Container
import org.igorv8836.bdui.contract.Layout
import org.igorv8836.bdui.contract.RemoteScreen
import org.igorv8836.bdui.contract.TextElement
import org.igorv8836.bdui.contract.ContainerDirection
import org.igorv8836.bdui.runtime.ScreenRepository

object Fixtures {

    fun screenWithTextAndButton(): RemoteScreen {
        val action = ForwardAction(id = "continue", path = "/demo/next")
        return RemoteScreen(
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
    private val remoteScreen: RemoteScreen = Fixtures.screenWithTextAndButton(),
    private val delayMs: Long = 0,
) : ScreenRepository {

    override suspend fun fetch(screenId: String, params: Map<String, String>): Result<RemoteScreen> {
        if (delayMs > 0) {
            delay(delayMs)
        }
        return Result.success(remoteScreen.copy(id = screenId))
    }
}

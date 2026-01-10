package org.igorv8836.bdui.demo

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToIndex
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.igorv8836.bdui.contract.Container
import org.igorv8836.bdui.contract.ContainerDirection
import org.igorv8836.bdui.contract.Layout
import org.igorv8836.bdui.contract.RemoteScreen
import org.igorv8836.bdui.contract.Section
import org.igorv8836.bdui.contract.Sticky
import org.igorv8836.bdui.contract.SpacerElement
import org.igorv8836.bdui.contract.StickyMode
import org.igorv8836.bdui.contract.TextElement
import org.igorv8836.bdui.contract.TextStyle
import org.igorv8836.bdui.core.actions.ActionRegistry
import org.igorv8836.bdui.renderer.host.ScreenHost
import org.igorv8836.bdui.runtime.ScreenState
import org.igorv8836.bdui.runtime.ScreenStatus
import org.igorv8836.bdui.runtime.VariableStoreImpl
import org.igorv8836.bdui.testing.TestNavigator
import org.junit.Rule
import org.junit.Test

class StickySectionsUiTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun topSticky_always_isPinnedAtTopAfterScrollDown() {
        val screen = screenWithSections(
            sticky = Section(
                id = "sticky-top",
                content = stickyBlock("sticky-top-content", "TOP ALWAYS"),
                sticky = Sticky.top(),
            ),
        )

        setContent(screen)
        scrollToIndex(6)

        composeRule.onNodeWithText("TOP ALWAYS").assertIsDisplayed()
    }

    @Test
    fun topSticky_onScrollTowardsEdge_isShownOnlyWhenScrollingUp() {
        val screen = screenWithSections(
            sticky = Section(
                id = "sticky-top",
                content = stickyBlock("sticky-top-content", "TOP ON SCROLL"),
                sticky = Sticky.top(StickyMode.OnScrollTowardsEdge),
            ),
        )

        setContent(screen)
        scrollToIndex(10)

        composeRule.onAllNodesWithText("TOP ON SCROLL").assertCountEquals(0)

        scrollToIndex(2)
        composeRule.onNodeWithText("TOP ON SCROLL").assertIsDisplayed()

        scrollToIndex(10)
        composeRule.onAllNodesWithText("TOP ON SCROLL").assertCountEquals(0)
    }

    @Test
    fun bottomSticky_always_isPinnedAtBottomAfterScrollDown() {
        val screen = screenWithSections(
            sticky = Section(
                id = "sticky-bottom",
                content = stickyBlock("sticky-bottom-content", "BOTTOM ALWAYS"),
                sticky = Sticky.bottom(),
            ),
        )

        setContent(screen)
        scrollToIndex(6)

        composeRule.onNodeWithText("BOTTOM ALWAYS").assertIsDisplayed()
    }

    @Test
    fun bottomSticky_onScrollTowardsEdge_isShownOnlyWhenScrollingDown() {
        val screen = screenWithSections(
            sticky = Section(
                id = "sticky-bottom",
                content = stickyBlock("sticky-bottom-content", "BOTTOM ON SCROLL"),
                sticky = Sticky.bottom(StickyMode.OnScrollTowardsEdge),
            ),
        )

        setContent(screen)
        scrollToIndex(10)

        composeRule.onNodeWithText("BOTTOM ON SCROLL").assertIsDisplayed()

        scrollToIndex(2)
        composeRule.onAllNodesWithText("BOTTOM ON SCROLL").assertCountEquals(0)

        scrollToIndex(10)
        composeRule.onNodeWithText("BOTTOM ON SCROLL").assertIsDisplayed()
    }

    @Test
    fun topSticky_inTheMiddle_isPinnedAfterPassingItsSection() {
        val sticky = Section(
            id = "sticky-mid",
            content = stickyBlock("sticky-mid-content", "TOP MID"),
            sticky = Sticky.top(),
        )
        val screen = RemoteScreen(
            id = "sticky-mid",
            version = 1,
            layout = Layout(
                sections = buildList {
                    repeat(12) { index ->
                        add(fillerSection(index))
                    }
                    add(sticky)
                    repeat(30) { index ->
                        add(fillerSection(index + 100))
                    }
                },
            ),
        )

        setContent(screen)

        scrollToIndex(20)
        composeRule.onNodeWithText("TOP MID").assertIsDisplayed()
    }

    @Test
    fun topSticky_multipleStickySections_replaceEachOtherWhileScrolling() {
        val sticky1 = Section(
            id = "sticky-a",
            content = stickyBlock("sticky-a-content", "TOP A"),
            sticky = Sticky.top(),
        )
        val sticky2 = Section(
            id = "sticky-b",
            content = stickyBlock("sticky-b-content", "TOP B"),
            sticky = Sticky.top(),
        )
        val screen = RemoteScreen(
            id = "multi-sticky",
            version = 1,
            layout = Layout(
                sections = buildList {
                    add(sticky1) // index 0
                    repeat(40) { index ->
                        add(fillerSection(index))
                    }
                    add(sticky2) // index 41
                    repeat(30) { index ->
                        add(fillerSection(index + 100))
                    }
                },
            ),
        )

        setContent(screen)

        // Pass first sticky.
        scrollToIndex(10)
        composeRule.onNodeWithText("TOP A").assertIsDisplayed()
        composeRule.onAllNodesWithText("TOP B").assertCountEquals(0)

        // Pass second sticky; it replaces the first one.
        scrollToIndex(45)
        composeRule.onNodeWithText("TOP B").assertIsDisplayed()
        composeRule.onAllNodesWithText("TOP A").assertCountEquals(0)

        // Scroll back above the second sticky, so the first sticky becomes pinned again.
        scrollToIndex(10)

        composeRule.onNodeWithText("TOP A").assertIsDisplayed()
        composeRule.onAllNodesWithText("TOP B").assertCountEquals(0)
    }

    private fun setContent(screen: RemoteScreen) {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
        val variables = VariableStoreImpl(scope = scope)
        val registry = ActionRegistry(emptyMap())

        composeRule.setContent {
            ScreenHost(
                state = ScreenState(remoteScreen = screen, status = ScreenStatus.Ready, empty = false),
                actionRegistry = registry,
                variableStore = variables,
                navigator = TestNavigator,
            )
        }
    }

    private fun scrollToIndex(index: Int) {
        composeRule.onNode(hasScrollAction()).performScrollToIndex(index)
        composeRule.waitForIdle()
    }

    private fun screenWithSections(sticky: Section): RemoteScreen {
        val fillers = buildList {
            repeat(40) { index ->
                add(
                    Section(
                        id = "filler-$index",
                        content = fillerBlock("filler-content-$index", "Filler $index"),
                    ),
                )
            }
        }
        val sections = listOf(sticky) + fillers
        return RemoteScreen(
            id = "sticky",
            version = 1,
            layout = Layout(sections = sections),
        )
    }

    private fun fillerSection(index: Int): Section =
        Section(
            id = "filler-$index",
            content = fillerBlock("filler-content-$index", "Filler $index"),
        )

    private fun fillerBlock(id: String, title: String): Container =
        contentBlock(
            id = id,
            title = title,
            spacerHeight = 220f,
        )

    private fun stickyBlock(id: String, title: String): Container =
        contentBlock(
            id = id,
            title = title,
            spacerHeight = null,
        )

    private fun contentBlock(
        id: String,
        title: String,
        spacerHeight: Float?,
    ): Container =
        Container(
            id = id,
            direction = ContainerDirection.Column,
            children = buildList {
                add(TextElement(id = "$id-title", text = title, style = TextStyle.Body))
                if (spacerHeight != null) {
                    add(SpacerElement(id = "$id-spacer", height = spacerHeight))
                }
            },
        )
}

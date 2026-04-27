package se.kjellstrand.fieldshootingtimer.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import se.kjellstrand.fieldshootingtimer.R

class CommandListTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private fun string(resId: Int): String =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(resId)

    private fun render(hlIndex: Int) {
        composeTestRule.setContent { CommandList(hlIndex = hlIndex) }
    }

    @Test
    fun commandList_highlightsTenSecondsLeft_atIndex2() {
        render(hlIndex = Command.TenSecondsLeft.ordinal)

        composeTestRule.onNodeWithText(string(R.string.command_10_seconds))
            .assertIsSelected()
    }

    @Test
    fun commandList_highlightsReady_atIndex3() {
        render(hlIndex = Command.Ready.ordinal)

        composeTestRule.onNodeWithText(string(R.string.command_ready))
            .assertIsSelected()
    }

    @Test
    fun commandList_highlightsFire_atIndex4() {
        render(hlIndex = Command.Fire.ordinal)

        composeTestRule.onNodeWithText(string(R.string.command_fire))
            .assertIsSelected()
    }

    @Test
    fun commandList_highlightsVisitation_atIndex7() {
        render(hlIndex = Command.Visitation.ordinal)

        composeTestRule.onNodeWithText(string(R.string.command_inspection))
            .assertIsSelected()
    }

    @Test
    fun commandList_nonHighlightedRow_isNotSelected() {
        render(hlIndex = Command.Fire.ordinal)

        composeTestRule.onNodeWithText(string(R.string.command_ready))
            .assertIsNotSelected()
    }
}

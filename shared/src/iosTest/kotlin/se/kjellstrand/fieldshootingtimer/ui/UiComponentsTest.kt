package se.kjellstrand.fieldshootingtimer.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isSelected
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import se.kjellstrand.fieldshootingtimer.ui.theme.FieldShootingTimerTheme
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class UiComponentsTest {

    @Test
    fun playButton_notStarted_showsPlayIcon() = runComposeUiTest {
        setContent {
            FieldShootingTimerTheme(dynamicColor = false) {
                PlayButton({}, TimerRunningState.NotStarted, 300.dp)
            }
        }
        onNodeWithContentDescription("Play").assertIsDisplayed()
    }

    @Test
    fun playButton_running_showsStopIcon() = runComposeUiTest {
        setContent {
            FieldShootingTimerTheme(dynamicColor = false) {
                PlayButton({}, TimerRunningState.Running, 300.dp)
            }
        }
        onNodeWithContentDescription("Stop").assertIsDisplayed()
    }

    @Test
    fun playButton_finished_showsResetIcon() = runComposeUiTest {
        setContent {
            FieldShootingTimerTheme(dynamicColor = false) {
                PlayButton({}, TimerRunningState.Finished, 300.dp)
            }
        }
        onNodeWithContentDescription("Reset").assertIsDisplayed()
    }

    @Test
    fun playButton_click_invokesCallback() = runComposeUiTest {
        var clicks = 0
        setContent {
            FieldShootingTimerTheme(dynamicColor = false) {
                PlayButton({ clicks++ }, TimerRunningState.NotStarted, 300.dp)
            }
        }
        onNodeWithContentDescription("Play").performClick()
        assertTrue(clicks == 1, "expected exactly one click, got $clicks")
    }

    @Test
    fun commandList_highlightsExactlyOneItem() = runComposeUiTest {
        setContent {
            FieldShootingTimerTheme(dynamicColor = false) {
                CommandList(hlIndex = Command.entries.indexOf(Command.Fire))
            }
        }
        onAllNodes(isSelected()).assertCountEquals(1)
    }
}

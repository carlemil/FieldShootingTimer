package se.kjellstrand.fieldshootingtimer.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import se.kjellstrand.fieldshootingtimer.ui.theme.FieldShootingTimerTheme
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class PlayButtonTest {

    @Test
    fun `notStarted shows play icon`() = runComposeUiTest {
        setContent {
            FieldShootingTimerTheme(dynamicColor = false) {
                PlayButton({}, TimerRunningState.NotStarted, 300.dp)
            }
        }
        onNodeWithTag(PLAY_ICON_TAG, useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun `running shows stop icon`() = runComposeUiTest {
        setContent {
            FieldShootingTimerTheme(dynamicColor = false) {
                PlayButton({}, TimerRunningState.Running, 300.dp)
            }
        }
        onNodeWithTag(STOP_ICON_TAG, useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun `stopped shows reset icon`() = runComposeUiTest {
        setContent {
            FieldShootingTimerTheme(dynamicColor = false) {
                PlayButton({}, TimerRunningState.Stopped, 300.dp)
            }
        }
        onNodeWithTag(RESET_ICON_TAG, useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun `finished shows reset icon`() = runComposeUiTest {
        setContent {
            FieldShootingTimerTheme(dynamicColor = false) {
                PlayButton({}, TimerRunningState.Finished, 300.dp)
            }
        }
        onNodeWithTag(RESET_ICON_TAG, useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun `countdown shows digits and keeps the stop icon visible`() = runComposeUiTest {
        setContent {
            FieldShootingTimerTheme(dynamicColor = false) {
                PlayButton({}, TimerRunningState.Running, 300.dp, countdownSeconds = 43)
            }
        }
        onNodeWithTag(COUNTDOWN_TEXT_TAG, useUnmergedTree = true).assertTextEquals("43")
        onNodeWithTag(STOP_ICON_TAG, useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun `notStarted shows the shooting total beside the play icon`() = runComposeUiTest {
        setContent {
            FieldShootingTimerTheme(dynamicColor = false) {
                PlayButton({}, TimerRunningState.NotStarted, 300.dp, countdownSeconds = 8)
            }
        }
        onNodeWithTag(COUNTDOWN_TEXT_TAG, useUnmergedTree = true).assertTextEquals("8")
        onNodeWithTag(PLAY_ICON_TAG, useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun `no countdown digits without a countdown`() = runComposeUiTest {
        setContent {
            FieldShootingTimerTheme(dynamicColor = false) {
                PlayButton({}, TimerRunningState.Running, 300.dp, countdownSeconds = null)
            }
        }
        onNodeWithTag(COUNTDOWN_TEXT_TAG, useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun `click invokes callback exactly once`() = runComposeUiTest {
        var clicks = 0
        setContent {
            FieldShootingTimerTheme(dynamicColor = false) {
                PlayButton({ clicks++ }, TimerRunningState.NotStarted, 300.dp)
            }
        }
        onNodeWithTag(PLAY_BUTTON_TAG).performClick()
        assertEquals(1, clicks)
    }
}

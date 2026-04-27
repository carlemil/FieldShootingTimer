package se.kjellstrand.fieldshootingtimer.ui

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class PlayButtonTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun playButton_showsPlayIcon_whenNotStarted() {
        composeTestRule.setContent {
            PlayButton(
                onClickPlayButton = {},
                timerRunningState = TimerRunningState.NotStarted,
                timerSize = 300.dp
            )
        }
        composeTestRule.onNodeWithContentDescription("Play").assertExists()
    }

    @Test
    fun playButton_showsStopIcon_whenRunning() {
        composeTestRule.setContent {
            PlayButton(
                onClickPlayButton = {},
                timerRunningState = TimerRunningState.Running,
                timerSize = 300.dp
            )
        }
        composeTestRule.onNodeWithContentDescription("Stop").assertExists()
    }

    @Test
    fun playButton_showsResetIcon_whenStopped() {
        composeTestRule.setContent {
            PlayButton(
                onClickPlayButton = {},
                timerRunningState = TimerRunningState.Stopped,
                timerSize = 300.dp
            )
        }
        composeTestRule.onNodeWithContentDescription("Reset").assertExists()
    }

    @Test
    fun playButton_showsResetIcon_whenFinished() {
        composeTestRule.setContent {
            PlayButton(
                onClickPlayButton = {},
                timerRunningState = TimerRunningState.Finished,
                timerSize = 300.dp
            )
        }
        composeTestRule.onNodeWithContentDescription("Reset").assertExists()
    }

    @Test
    fun playButton_invokesCallback_onClick() {
        var clicks = 0
        composeTestRule.setContent {
            PlayButton(
                onClickPlayButton = { clicks++ },
                timerRunningState = TimerRunningState.NotStarted,
                timerSize = 300.dp
            )
        }
        composeTestRule.onNodeWithContentDescription("Play")
            .assertHasClickAction()
            .performClick()
        assertEquals(1, clicks)
    }
}

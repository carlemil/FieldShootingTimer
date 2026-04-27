package se.kjellstrand.fieldshootingtimer

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onRoot
import org.junit.Rule
import org.junit.Test
import se.kjellstrand.fieldshootingtimer.ui.TimerRunningState
import se.kjellstrand.fieldshootingtimer.ui.TimerViewModel

class MainScreenSmokeTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun mainScreen_rendersWithoutCrashing_inDefaultState() {
        val viewModel = TimerViewModel()
        composeTestRule.setContent { MainScreen(timerViewModel = viewModel) }

        composeTestRule.onRoot().assertExists()
        composeTestRule.onNodeWithContentDescription("Play").assertExists()
    }

    @Test
    fun mainScreen_showsStopIcon_whenStateIsRunning() {
        // Drive state directly rather than calling start(): start() spawns a
        // wall-clock-driven coroutine that never lets waitForIdle settle.
        // Timer-loop behavior is covered by TimerViewModelTimingTest.
        val viewModel = TimerViewModel()
        composeTestRule.setContent { MainScreen(timerViewModel = viewModel) }

        composeTestRule.runOnUiThread {
            viewModel.setTimerState(TimerRunningState.Running)
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("Stop").assertExists()
    }

    @Test
    fun mainScreen_showsResetIcon_whenStateIsFinished() {
        val viewModel = TimerViewModel()
        composeTestRule.setContent { MainScreen(timerViewModel = viewModel) }

        composeTestRule.runOnUiThread {
            viewModel.setTimerState(TimerRunningState.Finished)
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("Reset").assertExists()
    }
}

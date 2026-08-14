package se.kjellstrand.fieldshootingtimer

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runDesktopComposeUiTest
import se.kjellstrand.fieldshootingtimer.ui.PLAY_BUTTON_TAG
import se.kjellstrand.fieldshootingtimer.ui.TimerRunningState
import se.kjellstrand.fieldshootingtimer.ui.TimerViewModel
import se.kjellstrand.fieldshootingtimer.ui.theme.FieldShootingTimerTheme
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Full-screen smoke tests. jvm-only (not in the shared uiTest source set)
 * because forcing portrait/landscape needs the desktop-specific
 * runDesktopComposeUiTest(width, height) surface control.
 */
@OptIn(ExperimentalTestApi::class)
class MainScreenSmokeTest {

    @Test
    fun `portrait layout renders and play starts the timer`() =
        runDesktopComposeUiTest(width = 400, height = 800) {
            val vm = TimerViewModel()
            setContent {
                FieldShootingTimerTheme(dynamicColor = false) {
                    MainScreen(vm)
                }
            }
            onNodeWithTag(PLAY_BUTTON_TAG).assertIsDisplayed()
            onNodeWithTag(PLAY_BUTTON_TAG).performClick()
            assertEquals(TimerRunningState.Running, vm.uiStateFlow.value.timerRunningState)
        }

    @Test
    fun `landscape layout renders`() =
        runDesktopComposeUiTest(width = 800, height = 400) {
            val vm = TimerViewModel()
            setContent {
                FieldShootingTimerTheme(dynamicColor = false) {
                    MainScreen(vm)
                }
            }
            onNodeWithTag(PLAY_BUTTON_TAG).assertIsDisplayed()
        }
}

package se.kjellstrand.fieldshootingtimer.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import se.kjellstrand.fieldshootingtimer.domain.TimerMode
import se.kjellstrand.fieldshootingtimer.ui.theme.FieldShootingTimerTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class TimerWithPlayButtonTest {

    private val segmentDurations = listOf(7f, 3f, 5f, 3f, 4f, 2f)

    @Test
    fun `plus on the dial adds a thumb to the view model`() = runComposeUiTest {
        val vm = TimerViewModel()
        setContent {
            FieldShootingTimerTheme(dynamicColor = false) {
                TimerWithPlayButton(
                    timerViewModel = vm,
                    segmentDurations = segmentDurations,
                    onClickPlayButton = {},
                    timerRunningState = TimerRunningState.NotStarted,
                    timerSize = 300.dp
                )
            }
        }
        onNodeWithTag(TICKS_PLUS_TAG).performClick()
        assertEquals(1, vm.uiStateFlow.value.thumbValues.size)
        assertTrue(vm.uiStateFlow.value.thumbValues.first() in 11f..17f)
    }

    @Test
    fun `minus on the dial drops the last thumb from the view model`() = runComposeUiTest {
        val vm = TimerViewModel()
        vm.setThumbValues(listOf(12f, 14f))
        setContent {
            FieldShootingTimerTheme(dynamicColor = false) {
                TimerWithPlayButton(
                    timerViewModel = vm,
                    segmentDurations = segmentDurations,
                    onClickPlayButton = {},
                    timerRunningState = TimerRunningState.NotStarted,
                    timerSize = 300.dp
                )
            }
        }
        onNodeWithTag(TICKS_MINUS_TAG).performClick()
        assertEquals(listOf(12f), vm.uiStateFlow.value.thumbValues)
    }

    @Test
    fun `negative time shows the countdown seconds below the play button`() = runComposeUiTest {
        val vm = TimerViewModel()
        vm.setTimerMode(TimerMode.Competition)
        vm.setCurrentTime(-42.3f)
        vm.setTimerState(TimerRunningState.Running)
        setContent {
            FieldShootingTimerTheme(dynamicColor = false) {
                TimerWithPlayButton(
                    timerViewModel = vm,
                    segmentDurations = segmentDurations,
                    onClickPlayButton = {},
                    timerRunningState = TimerRunningState.Running,
                    timerSize = 300.dp
                )
            }
        }
        onNodeWithTag(COUNTDOWN_TEXT_TAG, useUnmergedTree = true).assertTextEquals("43")
    }

    @Test
    fun `no countdown text at non-negative time`() = runComposeUiTest {
        val vm = TimerViewModel()
        setContent {
            FieldShootingTimerTheme(dynamicColor = false) {
                TimerWithPlayButton(
                    timerViewModel = vm,
                    segmentDurations = segmentDurations,
                    onClickPlayButton = {},
                    timerRunningState = TimerRunningState.NotStarted,
                    timerSize = 300.dp
                )
            }
        }
        onNodeWithTag(COUNTDOWN_TEXT_TAG, useUnmergedTree = true).assertDoesNotExist()
    }
}

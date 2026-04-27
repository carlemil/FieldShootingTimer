package se.kjellstrand.fieldshootingtimer.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SettingsPanelTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private fun render(viewModel: TimerViewModel) {
        // shootingDuration default = 5; range = 11..(5+10+3-1)=11..17
        // segmentDurations = [7,3,5,3,4,2]
        // SettingsPanel relies on a parent Column from PortraitLayout/
        // LandscapeLayout — without it, its children overlap and the
        // CommandList obscures the +/- click targets.
        composeTestRule.setContent {
            Column {
                SettingsPanel(
                    timerViewModel = viewModel,
                    range = 11..17,
                    segmentDurations = listOf(7f, 3f, 5f, 3f, 4f, 2f)
                )
            }
        }
    }

    @Test
    fun settingsPanel_plusButton_addsThumbToViewModel() {
        val vm = TimerViewModel()
        render(vm)

        composeTestRule.onNodeWithText("+").performClick()
        composeTestRule.waitForIdle()

        assertEquals(1, vm.uiStateFlow.value.thumbValues.size)
    }

    @Test
    fun settingsPanel_minusButton_removesLastThumb() {
        val vm = TimerViewModel().apply { setThumbValues(listOf(12f, 15f)) }
        render(vm)

        composeTestRule.onNodeWithText("-").performClick()
        composeTestRule.waitForIdle()

        assertEquals(listOf(12f), vm.uiStateFlow.value.thumbValues)
    }

    @Test
    fun settingsPanel_minusButton_isNoopWhenEmpty() {
        val vm = TimerViewModel()
        render(vm)

        composeTestRule.onNodeWithText("-").performClick()
        composeTestRule.waitForIdle()

        assertEquals(emptyList<Float>(), vm.uiStateFlow.value.thumbValues)
    }

    @Test
    fun settingsPanel_plusButton_disabled_whenTimerRunning() {
        val vm = TimerViewModel().apply { setTimerState(TimerRunningState.Running) }
        render(vm)

        composeTestRule.onNodeWithText("+").performClick()
        composeTestRule.waitForIdle()

        // When the timer is Running, the +/- handlers are gated by `enabled`
        // (SettingsPanel: enabled = state == NotStarted), so no thumb is added.
        assertEquals(emptyList<Float>(), vm.uiStateFlow.value.thumbValues)
    }
}

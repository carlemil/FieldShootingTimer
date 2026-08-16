package se.kjellstrand.fieldshootingtimer.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import se.kjellstrand.fieldshootingtimer.domain.Command
import se.kjellstrand.fieldshootingtimer.domain.TimerMode
import se.kjellstrand.fieldshootingtimer.ui.theme.FieldShootingTimerTheme
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class SettingsPanelTest {

    private val segmentDurations = listOf(7f, 3f, 5f, 3f, 4f, 2f)

    @Test
    fun `training mode hides the preparation commands`() = runComposeUiTest {
        val vm = TimerViewModel()
        vm.setTimerMode(TimerMode.Training)
        setContent {
            FieldShootingTimerTheme(dynamicColor = false) {
                Column {
                    SettingsPanel(vm, segmentDurations)
                }
            }
        }
        onNodeWithTag("$COMMAND_LIST_ROW_TAG${Command.Load.name}").assertDoesNotExist()
        onNodeWithTag("$COMMAND_LIST_ROW_TAG${Command.AllReady.name}").assertDoesNotExist()
        onNodeWithTag("$COMMAND_LIST_ROW_TAG${Command.Fire.name}").assertExists()
    }

    @Test
    fun `competition mode shows the preparation commands and starts on Load`() = runComposeUiTest {
        val vm = TimerViewModel()
        vm.setTimerMode(TimerMode.Competition)
        setContent {
            FieldShootingTimerTheme(dynamicColor = false) {
                Column {
                    SettingsPanel(vm, segmentDurations)
                }
            }
        }
        onNodeWithTag("$COMMAND_LIST_ROW_TAG${Command.AllReady.name}").assertExists()
        onNodeWithTag("$COMMAND_LIST_ROW_TAG${Command.Load.name}").assertIsSelected()
    }

    @Test
    fun `tapping a command row parks the timer at that command's start`() = runComposeUiTest {
        val vm = TimerViewModel()
        vm.setShootingTime(5f)
        setContent {
            FieldShootingTimerTheme(dynamicColor = false) {
                Column {
                    SettingsPanel(vm, segmentDurations)
                }
            }
        }
        onNodeWithTag("$COMMAND_LIST_ROW_TAG${Command.Fire.name}").performClick()
        assertEquals(10f, vm.uiStateFlow.value.currentTime)
        assertEquals(TimerRunningState.NotStarted, vm.uiStateFlow.value.timerRunningState)
    }
}

package se.kjellstrand.fieldshootingtimer.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import se.kjellstrand.fieldshootingtimer.ui.theme.FieldShootingTimerTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class SettingsPanelTest {

    @Test
    fun `plus adds a thumb to the view model`() = runComposeUiTest {
        val vm = TimerViewModel()
        setContent {
            FieldShootingTimerTheme(dynamicColor = false) {
                Column {
                    SettingsPanel(vm, 11..17, listOf(7f, 3f, 5f, 3f, 4f, 2f))
                }
            }
        }
        onNodeWithTag(TICKS_PLUS_TAG).performClick()
        assertEquals(1, vm.uiStateFlow.value.thumbValues.size)
        assertTrue(vm.uiStateFlow.value.thumbValues.first() in 11f..17f)
    }

    @Test
    fun `minus drops the last thumb from the view model`() = runComposeUiTest {
        val vm = TimerViewModel()
        vm.setThumbValues(listOf(12f, 14f))
        setContent {
            FieldShootingTimerTheme(dynamicColor = false) {
                Column {
                    SettingsPanel(vm, 11..17, listOf(7f, 3f, 5f, 3f, 4f, 2f))
                }
            }
        }
        onNodeWithTag(TICKS_MINUS_TAG).performClick()
        assertEquals(listOf(12f), vm.uiStateFlow.value.thumbValues)
    }
}

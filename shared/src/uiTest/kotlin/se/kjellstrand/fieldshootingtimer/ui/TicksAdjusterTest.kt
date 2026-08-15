package se.kjellstrand.fieldshootingtimer.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import se.kjellstrand.fieldshootingtimer.ui.theme.FieldShootingTimerTheme
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class TicksAdjusterTest {

    @Test
    fun `plus and minus invoke their callbacks when enabled`() = runComposeUiTest {
        var plus = 0
        var minus = 0
        setContent {
            FieldShootingTimerTheme(dynamicColor = false) {
                TicksAdjuster(
                    enabled = true,
                    setThumbValuesMinusOne = { minus++ },
                    setThumbValuesPlusOne = { plus++ }
                )
            }
        }
        onNodeWithTag(TICKS_PLUS_TAG).performClick()
        onNodeWithTag(TICKS_MINUS_TAG).performClick()
        assertEquals(1, plus)
        assertEquals(1, minus)
    }

    @Test
    fun `plus and minus are inert when disabled`() = runComposeUiTest {
        var plus = 0
        var minus = 0
        setContent {
            FieldShootingTimerTheme(dynamicColor = false) {
                TicksAdjuster(
                    enabled = false,
                    setThumbValuesMinusOne = { minus++ },
                    setThumbValuesPlusOne = { plus++ }
                )
            }
        }
        onNodeWithTag(TICKS_PLUS_TAG).performClick()
        onNodeWithTag(TICKS_MINUS_TAG).performClick()
        assertEquals(0, plus)
        assertEquals(0, minus)
    }
}

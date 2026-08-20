package se.kjellstrand.fieldshootingtimer.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import se.kjellstrand.fieldshootingtimer.ui.theme.FieldShootingTimerTheme
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class MarkConfirmationTest {

    @Test
    fun `mark button fires onMark`() = runComposeUiTest {
        var marks = 0
        var closes = 0
        setContent {
            FieldShootingTimerTheme(dynamicColor = false) {
                MarkConfirmationOverlay(onMark = { marks++ }, onClose = { closes++ })
            }
        }
        onNodeWithTag(MARK_CONFIRM_MARK_TAG).performClick()
        assertEquals(1, marks)
        assertEquals(0, closes)
    }

    @Test
    fun `close fires onClose without marking`() = runComposeUiTest {
        var marks = 0
        var closes = 0
        setContent {
            FieldShootingTimerTheme(dynamicColor = false) {
                MarkConfirmationOverlay(onMark = { marks++ }, onClose = { closes++ })
            }
        }
        onNodeWithTag(MARK_CONFIRM_CLOSE_TAG).performClick()
        assertEquals(0, marks)
        assertEquals(1, closes)
    }
}

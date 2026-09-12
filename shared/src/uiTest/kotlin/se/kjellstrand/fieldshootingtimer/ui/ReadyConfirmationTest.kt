package se.kjellstrand.fieldshootingtimer.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import se.kjellstrand.fieldshootingtimer.ui.theme.FieldShootingTimerTheme
import kotlin.test.Test
import kotlin.test.assertEquals

/** Drives [LoadConfirmationOverlay] and [ReadyConfirmationOverlay] with plain callbacks. */
@OptIn(ExperimentalTestApi::class)
class ReadyConfirmationTest {

    @Test
    fun `continue and close fire their callbacks`() = runComposeUiTest {
        var continues = 0
        var closes = 0
        setContent {
            FieldShootingTimerTheme(dynamicColor = false) {
                ReadyConfirmationOverlay(onContinue = { continues++ }, onClose = { closes++ })
            }
        }
        onNodeWithTag(READY_CONTINUE_TAG).performClick()
        assertEquals(1, continues)
        onNodeWithTag(READY_CLOSE_TAG).performClick()
        assertEquals(1, closes)
        // Press outside the card: nothing happens, the overlay stays modal.
        onNodeWithTag(READY_CONFIRM_TAG).performClick()
        assertEquals(1, continues)
        assertEquals(1, closes)
    }

    @Test
    fun `the Ladda dialog has its own tags`() = runComposeUiTest {
        var continues = 0
        var closes = 0
        setContent {
            FieldShootingTimerTheme(dynamicColor = false) {
                LoadConfirmationOverlay(onContinue = { continues++ }, onClose = { closes++ })
            }
        }
        onNodeWithTag(LOAD_CONTINUE_TAG).performClick()
        onNodeWithTag(LOAD_CLOSE_TAG).performClick()
        assertEquals(1, continues)
        assertEquals(1, closes)
    }

    @Test
    fun `the Patron ur dialog has its own tags`() = runComposeUiTest {
        var continues = 0
        var closes = 0
        setContent {
            FieldShootingTimerTheme(dynamicColor = false) {
                UnloadConfirmationOverlay(onContinue = { continues++ }, onClose = { closes++ })
            }
        }
        onNodeWithTag(UNLOAD_CONTINUE_TAG).performClick()
        onNodeWithTag(UNLOAD_CLOSE_TAG).performClick()
        assertEquals(1, continues)
        assertEquals(1, closes)
    }

    @Test
    fun `the Visitation dialog has its own tags`() = runComposeUiTest {
        var continues = 0
        var closes = 0
        setContent {
            FieldShootingTimerTheme(dynamicColor = false) {
                VisitationConfirmationOverlay(onContinue = { continues++ }, onClose = { closes++ })
            }
        }
        onNodeWithTag(VISITATION_CONTINUE_TAG).performClick()
        onNodeWithTag(VISITATION_CLOSE_TAG).performClick()
        assertEquals(1, continues)
        assertEquals(1, closes)
    }
}

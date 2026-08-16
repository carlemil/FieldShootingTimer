package se.kjellstrand.fieldshootingtimer.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import se.kjellstrand.fieldshootingtimer.ui.theme.FieldShootingTimerTheme
import kotlin.test.Test
import kotlin.test.assertEquals

/** Drives [ReadyConfirmationOverlay] directly with plain callbacks. */
@OptIn(ExperimentalTestApi::class)
class ReadyConfirmationTest {

    @Test
    fun `continue fires its callback`() = runComposeUiTest {
        var continues = 0
        var agains = 0
        setContent {
            FieldShootingTimerTheme(dynamicColor = false) {
                ReadyConfirmationOverlay(
                    onContinue = { continues++ },
                    onAskAgain = { agains++ }
                )
            }
        }
        onNodeWithTag(READY_CONTINUE_TAG).performClick()
        assertEquals(1, continues)
        assertEquals(0, agains)
    }

    @Test
    fun `ask again fires its callback`() = runComposeUiTest {
        var continues = 0
        var agains = 0
        setContent {
            FieldShootingTimerTheme(dynamicColor = false) {
                ReadyConfirmationOverlay(
                    onContinue = { continues++ },
                    onAskAgain = { agains++ }
                )
            }
        }
        onNodeWithTag(READY_AGAIN_TAG).performClick()
        assertEquals(0, continues)
        assertEquals(1, agains)
    }

    @Test
    fun `the scrim swallows presses`() = runComposeUiTest {
        var continues = 0
        setContent {
            FieldShootingTimerTheme(dynamicColor = false) {
                ReadyConfirmationOverlay(onContinue = { continues++ }, onAskAgain = {})
            }
        }
        // Press outside the card: nothing happens, the overlay stays modal.
        onNodeWithTag(READY_CONFIRM_TAG).performClick()
        assertEquals(0, continues)
    }
}

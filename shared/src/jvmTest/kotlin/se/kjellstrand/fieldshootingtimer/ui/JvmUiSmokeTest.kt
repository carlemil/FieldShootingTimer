package se.kjellstrand.fieldshootingtimer.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import se.kjellstrand.fieldshootingtimer.ui.theme.FieldShootingTimerTheme
import kotlin.test.Test

/**
 * Smoke test proving the jvm target can render shared composables headlessly.
 * The real UI suite lives in the shared uiTest source set.
 */
@OptIn(ExperimentalTestApi::class)
class JvmUiSmokeTest {

    @Test
    fun playButton_rendersHeadlessly() = runComposeUiTest {
        setContent {
            FieldShootingTimerTheme(dynamicColor = false) {
                PlayButton({}, TimerRunningState.NotStarted, 300.dp)
            }
        }
        onNodeWithContentDescription("Play").assertIsDisplayed()
    }
}

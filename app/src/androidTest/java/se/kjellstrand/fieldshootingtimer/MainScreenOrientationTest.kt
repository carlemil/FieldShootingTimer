package se.kjellstrand.fieldshootingtimer

import android.content.res.Configuration
import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onRoot
import org.junit.Rule
import org.junit.Test
import se.kjellstrand.fieldshootingtimer.ui.TimerViewModel

class MainScreenOrientationTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private fun configurationFor(orientation: Int): Configuration =
        Configuration().apply { this.orientation = orientation }

    @Test
    fun mainScreen_rendersWithoutCrashing_inPortrait() {
        val viewModel = TimerViewModel()
        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalConfiguration provides configurationFor(Configuration.ORIENTATION_PORTRAIT)
            ) {
                MainScreen(timerViewModel = viewModel)
            }
        }

        composeTestRule.onRoot().assertExists()
        composeTestRule.onNodeWithContentDescription("Play").assertExists()
    }

    @Test
    fun mainScreen_rendersWithoutCrashing_inLandscape() {
        val viewModel = TimerViewModel()
        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalConfiguration provides configurationFor(Configuration.ORIENTATION_LANDSCAPE)
            ) {
                MainScreen(timerViewModel = viewModel)
            }
        }

        composeTestRule.onRoot().assertExists()
        composeTestRule.onNodeWithContentDescription("Play").assertExists()
    }
}

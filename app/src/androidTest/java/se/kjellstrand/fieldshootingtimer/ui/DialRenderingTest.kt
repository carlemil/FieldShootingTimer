package se.kjellstrand.fieldshootingtimer.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test

/**
 * Smoke tests for the custom Canvas dial composables. Verifies they
 * render without crashing for the segment durations the timer can
 * produce in production. Pixel-level rendering is not asserted —
 * DialGeometryTest already covers the underlying angle math.
 */
class DialRenderingTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private fun defaultColors(): List<Color> = listOf(
        Color.LightGray, Color.LightGray, Color.Green,
        Color.Yellow, Color.Red, Color.LightGray
    )

    /** segments derived the same way TimerViewModel.buildSegmentDurations does. */
    private fun segmentsFor(shooting: Float): List<Float> = listOf(
        Command.TenSecondsLeft.duration.toFloat(), // 7
        Command.Ready.duration.toFloat(),          // 3
        shooting,
        Command.CeaseFire.duration.toFloat(),      // 3
        Command.UnloadWeapon.duration.toFloat(),   // 4
        Command.Visitation.duration.toFloat()      // 2
    )

    @Composable
    private fun DefaultDial(shooting: Float, ticks: List<Float> = emptyList()) {
        DecoratedDial(
            segmentColors = defaultColors(),
            segments = segmentsFor(shooting),
            ticks = ticks,
            size = 300.dp
        )
    }

    @Test
    fun decoratedDial_rendersWithDefaultShootingDuration_5s() {
        composeTestRule.setContent {
            MaterialTheme { Box { DefaultDial(shooting = 5f) } }
        }
        composeTestRule.onRoot().assertExists()
    }

    @Test
    fun decoratedDial_rendersWithMinimumShootingDuration_1s() {
        composeTestRule.setContent {
            MaterialTheme { Box { DefaultDial(shooting = 1f) } }
        }
        composeTestRule.onRoot().assertExists()
    }

    @Test
    fun decoratedDial_rendersWithMaximumShootingDuration_27s() {
        // 27 is the upper bound used by ShootTimeAdjuster's slider range.
        composeTestRule.setContent {
            MaterialTheme { Box { DefaultDial(shooting = 27f) } }
        }
        composeTestRule.onRoot().assertExists()
    }

    @Test
    fun decoratedDial_rendersWithUserTicks_withoutCrashing() {
        // Badge text is drawn via nativeCanvas.drawText (no semantics);
        // the placement math is covered by TickBadgePlacementsTest. Here
        // we only verify the composable renders with ticks present.
        composeTestRule.setContent {
            MaterialTheme { Box { DefaultDial(shooting = 10f, ticks = listOf(13f, 17f)) } }
        }
        composeTestRule.onRoot().assertExists()
    }

    @Test
    fun dialHand_rendersAtZeroProgress() {
        composeTestRule.setContent {
            DialHand(currentTime = 0f, totalTime = 24f, size = 300.dp)
        }
        composeTestRule.onRoot().assertExists()
    }

    @Test
    fun dialHand_rendersAtFullProgress() {
        composeTestRule.setContent {
            DialHand(currentTime = 24f, totalTime = 24f, size = 300.dp)
        }
        composeTestRule.onRoot().assertExists()
    }
}

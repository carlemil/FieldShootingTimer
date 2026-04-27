package se.kjellstrand.fieldshootingtimer.ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ShootTimeAdjusterTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    /**
     * Hosts ShootTimeAdjuster with a tracked-mutable shooting duration,
     * the way SettingsPanel/MainScreen wire it up. The lambda forwards
     * onValueChange to a state holder, mirroring TimerViewModel's
     * setShootingTime path (rounded to int).
     */
    private fun renderTracked(initial: Float = 5f): () -> Float {
        var current by mutableStateOf(initial)
        composeTestRule.setContent {
            ShootTimeAdjuster(
                shootingDuration = current,
                enabled = true,
                onValueChange = { values ->
                    current = if (values.isEmpty()) 0f else values.first()
                }
            )
        }
        return { current }
    }

    /**
     * MultiThumbSlider's gesture detector is on a Box positioned at the
     * thumb's offset, not on the whole slider — so a swipe starting at
     * the slider's left edge misses. This computes the thumb's x in slider
     * pixels for `value` in range 1..27 (matches MultiThumbSlider's
     * toThumbOffset math).
     */
    private fun thumbX(value: Float, sliderWidthPx: Float): Float {
        val rangeFirst = 1f
        val rangeLast = 27f
        val segmentWidth = sliderWidthPx / (rangeLast + 1f - rangeFirst)
        val firstAndLastSegmentWidth = segmentWidth / 2f
        val trackWidth = sliderWidthPx - segmentWidth
        return ((value - rangeFirst) / (rangeLast - rangeFirst)) * trackWidth +
                firstAndLastSegmentWidth
    }

    @Test
    fun shootTimeAdjuster_dragRight_increasesShootingDuration() {
        val read = renderTracked(initial = 5f)

        composeTestRule.onNodeWithTag(SHOOT_TIME_SLIDER_TAG).performTouchInput {
            val startX = thumbX(5f, width.toFloat())
            swipe(
                start = Offset(startX, height / 2f),
                end = Offset((width - 1).toFloat(), height / 2f),
                durationMillis = 200
            )
        }
        composeTestRule.waitForIdle()

        assertTrue(
            "duration should increase after right drag; was ${read()}",
            read() > 5f
        )
    }

    @Test
    fun shootTimeAdjuster_dragLeft_decreasesShootingDuration() {
        val read = renderTracked(initial = 20f)

        composeTestRule.onNodeWithTag(SHOOT_TIME_SLIDER_TAG).performTouchInput {
            val startX = thumbX(20f, width.toFloat())
            swipe(
                start = Offset(startX, height / 2f),
                end = Offset(1f, height / 2f),
                durationMillis = 200
            )
        }
        composeTestRule.waitForIdle()

        assertTrue(
            "duration should decrease after left drag; was ${read()}",
            read() < 20f
        )
    }

    @Test
    fun shootTimeAdjuster_clampsAtRangeMax_onLargeRightSwipe() {
        val read = renderTracked(initial = 25f)

        composeTestRule.onNodeWithTag(SHOOT_TIME_SLIDER_TAG).performTouchInput {
            val startX = thumbX(25f, width.toFloat())
            swipe(
                start = Offset(startX, height / 2f),
                end = Offset((width * 2).toFloat(), height / 2f),
                durationMillis = 200
            )
        }
        composeTestRule.waitForIdle()

        // MultiThumbSlider in ShootTimeAdjuster uses range 1..27.
        assertTrue("duration must stay <= 27; was ${read()}", read() <= 27f)
    }

    @Test
    fun shootTimeAdjuster_clampsAtRangeMin_onLargeLeftSwipe() {
        val read = renderTracked(initial = 3f)

        composeTestRule.onNodeWithTag(SHOOT_TIME_SLIDER_TAG).performTouchInput {
            val startX = thumbX(3f, width.toFloat())
            swipe(
                start = Offset(startX, height / 2f),
                end = Offset((-width).toFloat(), height / 2f),
                durationMillis = 200
            )
        }
        composeTestRule.waitForIdle()

        assertTrue("duration must stay >= 1; was ${read()}", read() >= 1f)
    }

    @Test
    fun shootTimeAdjuster_dragFires_callbackChangesValue() {
        val read = renderTracked(initial = 10f)

        composeTestRule.onNodeWithTag(SHOOT_TIME_SLIDER_TAG).performTouchInput {
            val startX = thumbX(10f, width.toFloat())
            swipe(
                start = Offset(startX, height / 2f),
                end = Offset((width - 1).toFloat(), height / 2f),
                durationMillis = 200
            )
        }
        composeTestRule.waitForIdle()

        assertNotEquals(10f, read())
    }
}

package se.kjellstrand.fieldshootingtimer.ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class MultiThumbSliderTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val sliderTag = "MultiThumbSlider"

    /**
     * MultiThumbSlider's gesture detector lives on a Box positioned at
     * each thumb's offset (not the whole slider area). Replicates the
     * math from MultiThumbSlider.toThumbOffset.
     */
    private fun thumbX(value: Float, range: IntRange, sliderWidthPx: Float): Float {
        val segmentWidth = sliderWidthPx / (range.last + 1f - range.first)
        val firstAndLastSegmentWidth = segmentWidth / 2f
        val trackWidth = sliderWidthPx - segmentWidth
        return ((value - range.first) / (range.last - range.first)) * trackWidth +
                firstAndLastSegmentWidth
    }

    private fun render(
        range: IntRange,
        initial: List<Float>,
        enabled: Boolean = true,
        onDragEnd: () -> Unit = {}
    ): () -> List<Float> {
        var values by mutableStateOf(initial)
        composeTestRule.setContent {
            MultiThumbSlider(
                thumbValues = values,
                onHorizontalDragSetThumbValues = { values = it },
                onHorizontalDragRoundThumbValues = onDragEnd,
                range = range,
                enabled = enabled,
                modifier = Modifier.testTag(sliderTag)
            )
        }
        return { values }
    }

    @Test
    fun multiThumbSlider_dragsThumbForward_increasesValue() {
        val range = 1..21
        val read = render(range = range, initial = listOf(11f))

        composeTestRule.onNodeWithTag(sliderTag).performTouchInput {
            val startX = thumbX(11f, range, width.toFloat())
            swipe(
                start = Offset(startX, height / 2f),
                end = Offset((width - 1).toFloat(), height / 2f),
                durationMillis = 200
            )
        }
        composeTestRule.waitForIdle()

        assertTrue("value should increase; got ${read()}", read().single() > 11f)
    }

    @Test
    fun multiThumbSlider_dragsThumbBackward_decreasesValue() {
        val range = 1..21
        val read = render(range = range, initial = listOf(15f))

        composeTestRule.onNodeWithTag(sliderTag).performTouchInput {
            val startX = thumbX(15f, range, width.toFloat())
            swipe(
                start = Offset(startX, height / 2f),
                end = Offset(1f, height / 2f),
                durationMillis = 200
            )
        }
        composeTestRule.waitForIdle()

        assertTrue("value should decrease; got ${read()}", read().single() < 15f)
    }

    @Test
    fun multiThumbSlider_clampsAtRangeStart_onLargeLeftDrag() {
        val range = 1..21
        val read = render(range = range, initial = listOf(5f))

        composeTestRule.onNodeWithTag(sliderTag).performTouchInput {
            val startX = thumbX(5f, range, width.toFloat())
            swipe(
                start = Offset(startX, height / 2f),
                end = Offset((-width).toFloat(), height / 2f),
                durationMillis = 200
            )
        }
        composeTestRule.waitForIdle()

        // newOffset is coerced to 0..maxWidth in MultiThumbSlider,
        // mapping to range.first at the low end.
        assertEquals(1f, read().single(), 0.01f)
    }

    @Test
    fun multiThumbSlider_clampsAtRangeEnd_onLargeRightDrag() {
        val range = 1..21
        val read = render(range = range, initial = listOf(15f))

        composeTestRule.onNodeWithTag(sliderTag).performTouchInput {
            val startX = thumbX(15f, range, width.toFloat())
            swipe(
                start = Offset(startX, height / 2f),
                end = Offset((width * 2).toFloat(), height / 2f),
                durationMillis = 200
            )
        }
        composeTestRule.waitForIdle()

        assertEquals(21f, read().single(), 0.01f)
    }

    @Test
    fun multiThumbSlider_dragsCorrectThumb_whenMultiplePresent() {
        val range = 1..21
        val read = render(range = range, initial = listOf(5f, 15f))

        composeTestRule.onNodeWithTag(sliderTag).performTouchInput {
            val startX = thumbX(15f, range, width.toFloat())
            swipe(
                start = Offset(startX, height / 2f),
                end = Offset((width - 1).toFloat(), height / 2f),
                durationMillis = 200
            )
        }
        composeTestRule.waitForIdle()

        val current = read()
        assertEquals("first thumb must be unchanged", 5f, current[0], 0.01f)
        assertTrue("second thumb must move right; got ${current[1]}", current[1] > 15f)
    }

    @Test
    fun multiThumbSlider_dragFires_callbackChangesValues() {
        val range = 1..21
        val read = render(range = range, initial = listOf(10f))

        composeTestRule.onNodeWithTag(sliderTag).performTouchInput {
            val startX = thumbX(10f, range, width.toFloat())
            swipe(
                start = Offset(startX, height / 2f),
                end = Offset((width - 1).toFloat(), height / 2f),
                durationMillis = 200
            )
        }
        composeTestRule.waitForIdle()

        assertNotEquals(10f, read().single())
    }

    @Test
    fun multiThumbSlider_dragEnd_invokesRoundCallback() {
        val range = 1..21
        var dragEndCalls = 0
        render(range = range, initial = listOf(10f), onDragEnd = { dragEndCalls++ })

        composeTestRule.onNodeWithTag(sliderTag).performTouchInput {
            val startX = thumbX(10f, range, width.toFloat())
            swipe(
                start = Offset(startX, height / 2f),
                end = Offset((width - 1).toFloat(), height / 2f),
                durationMillis = 200
            )
        }
        composeTestRule.waitForIdle()

        assertTrue("onDragEnd round callback should fire; calls=$dragEndCalls", dragEndCalls >= 1)
    }

    @Test
    fun multiThumbSlider_disabled_doesNotMoveThumb() {
        val range = 1..21
        val read = render(range = range, initial = listOf(10f), enabled = false)

        composeTestRule.onNodeWithTag(sliderTag).performTouchInput {
            val startX = thumbX(10f, range, width.toFloat())
            swipe(
                start = Offset(startX, height / 2f),
                end = Offset((width - 1).toFloat(), height / 2f),
                durationMillis = 200
            )
        }
        composeTestRule.waitForIdle()

        // When enabled = false, the per-thumb gesture Boxes are not
        // composed at all (see MultiThumbSlider's `if (enabled)` block),
        // so drags simply have no target.
        assertEquals(10f, read().single(), 0.01f)
    }
}

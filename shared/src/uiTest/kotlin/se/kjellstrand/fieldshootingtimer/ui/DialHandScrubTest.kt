package se.kjellstrand.fieldshootingtimer.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.TouchInjectionScope
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import se.kjellstrand.fieldshootingtimer.ui.theme.FieldShootingTimerTheme
import kotlin.math.roundToInt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Drives [DialGestureOverlay]'s hand-scrub gesture directly, mirroring
 * [DialTicksDragTest]'s no-ViewModel setup. Dial total 18s, Fire 10..15.
 * Hand grabs land well inside the ring band (radius factor 0.3) so the
 * tick hit test never competes with them.
 */
@OptIn(ExperimentalTestApi::class)
class DialHandScrubTest {

    private val gap = 30f
    private val total = 18f

    // A point on the hand's line (inside the ring band) at [time]'s angle.
    private fun TouchInjectionScope.handPointAt(time: Float): Offset {
        val radius = (width / 2f) * 0.3f
        return polarToCartesian(center, radius, DialGeometry.tickAngle(time, total, gap))
    }

    // A point on the dial ring at the angle where [tickValue]'s marker sits.
    private fun TouchInjectionScope.ringPointAt(tickValue: Float): Offset {
        val ringRadius = (width / 2f) * 0.85f
        return polarToCartesian(center, ringRadius, DialGeometry.tickAngle(tickValue, total, gap))
    }

    private class ScrubState(initial: Float) {
        var time by mutableStateOf(initial)
        var ticks by mutableStateOf(listOf<Float>())

        fun scrub(value: Float) {
            time = value
        }

        fun replaceTicks(values: List<Float>) {
            ticks = values
        }

        fun roundTicks() {
            ticks = ticks.map { it.roundToInt().toFloat() }
        }
    }

    private fun androidx.compose.ui.test.ComposeUiTest.setOverlayContent(
        state: ScrubState,
        editEnabled: Boolean = true,
        scrubEnabled: Boolean = true
    ) {
        setContent {
            FieldShootingTimerTheme(dynamicColor = false) {
                Box(Modifier.size(300.dp)) {
                    DialGestureOverlay(
                        size = 300.dp,
                        ticks = state.ticks,
                        ticksMax = total.toInt(),
                        range = 11..17,
                        fireStart = 10f,
                        fireDuration = 5f,
                        currentTime = state.time,
                        gapAngleDegrees = gap,
                        ringThickness = 60.dp,
                        editEnabled = editEnabled,
                        scrubEnabled = scrubEnabled,
                        onDragSetTicks = state::replaceTicks,
                        onDragRoundTicks = state::roundTicks,
                        onPinchSetShootingDuration = {},
                        onScrub = state::scrub
                    )
                }
            }
        }
    }

    @Test
    fun `dragging the hand scrubs the time and snaps to a whole second`() = runComposeUiTest {
        val state = ScrubState(0f)
        setOverlayContent(state)
        onNodeWithTag(DIAL_GESTURE_TAG).performTouchInput {
            down(handPointAt(0f))
            listOf(3f, 6.5f, 9.6f).forEach { moveTo(handPointAt(it)) }
            up()
        }
        assertEquals(10f, state.time)
    }

    @Test
    fun `hand drag works even when editing is disabled`() = runComposeUiTest {
        // Paused mid-run: ticks/pinch are locked but the hand still scrubs.
        val state = ScrubState(12f)
        setOverlayContent(state, editEnabled = false, scrubEnabled = true)
        onNodeWithTag(DIAL_GESTURE_TAG).performTouchInput {
            down(handPointAt(12f))
            listOf(10f, 8.4f).forEach { moveTo(handPointAt(it)) }
            up()
        }
        assertEquals(8f, state.time)
    }

    @Test
    fun `hand drag is ignored while scrubbing is disabled`() = runComposeUiTest {
        val state = ScrubState(0f)
        setOverlayContent(state, scrubEnabled = false)
        onNodeWithTag(DIAL_GESTURE_TAG).performTouchInput {
            down(handPointAt(0f))
            listOf(5f, 9f).forEach { moveTo(handPointAt(it)) }
            up()
        }
        assertEquals(0f, state.time)
    }

    @Test
    fun `a grab away from the hand scrubs nothing`() = runComposeUiTest {
        val state = ScrubState(0f)
        setOverlayContent(state)
        // The hand points at 0; grab the dial face at 9's angle instead.
        onNodeWithTag(DIAL_GESTURE_TAG).performTouchInput {
            down(handPointAt(9f))
            listOf(12f, 15f).forEach { moveTo(handPointAt(it)) }
            up()
        }
        assertEquals(0f, state.time)
    }

    @Test
    fun `a tick grab wins over the hand when they coincide`() = runComposeUiTest {
        val state = ScrubState(14f)
        state.ticks = listOf(14f)
        setOverlayContent(state)
        onNodeWithTag(DIAL_GESTURE_TAG).performTouchInput {
            down(ringPointAt(14f))
            listOf(15f, 16f).forEach { moveTo(ringPointAt(it)) }
            up()
        }
        assertEquals(listOf(16f), state.ticks)
        assertTrue(state.time == 14f, "the hand must not scrub during a tick drag, got ${state.time}")
    }
}

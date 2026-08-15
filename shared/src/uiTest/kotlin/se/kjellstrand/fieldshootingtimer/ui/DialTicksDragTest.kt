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

/**
 * Drives [DialGestureOverlay] directly with explicit parameters and plain
 * Compose state — deliberately no ViewModel flows, whose collection timing
 * differs per platform (the iOS test host processes them later than the
 * JVM one, which made ShootTimer-based versions of these tests flaky).
 * The dial matches the default plan: dial total 18s (through CeaseFire), Fire 10..15, range 11..17.
 */
@OptIn(ExperimentalTestApi::class)
class DialTicksDragTest {

    private val gap = 30f
    private val total = 18f

    // A point on the dial ring at the angle where [tickValue]'s marker sits.
    private fun TouchInjectionScope.ringPointAt(tickValue: Float): Offset {
        val ringRadius = (width / 2f) * 0.85f
        return polarToCartesian(center, ringRadius, DialGeometry.tickAngle(tickValue, total, gap))
    }

    private class TicksState(initial: List<Float>) {
        var ticks by mutableStateOf(initial)

        fun set(values: List<Float>) {
            ticks = values
        }

        fun round() {
            ticks = ticks.map { it.roundToInt().toFloat() }
        }
    }

    private fun androidx.compose.ui.test.ComposeUiTest.setOverlayContent(
        state: TicksState,
        enabled: Boolean = true
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
                        gapAngleDegrees = gap,
                        ringThickness = 60.dp,
                        enabled = enabled,
                        onDragSetTicks = state::set,
                        onDragRoundTicks = state::round,
                        onPinchSetShootingDuration = {}
                    )
                }
            }
        }
    }

    @Test
    fun `dragging a dial tick moves it and rounds on release`() = runComposeUiTest {
        val state = TicksState(listOf(14f))
        setOverlayContent(state)
        onNodeWithTag(DIAL_GESTURE_TAG).performTouchInput {
            down(ringPointAt(14f))
            listOf(14.5f, 15f, 15.5f, 16f).forEach { moveTo(ringPointAt(it)) }
            up()
        }
        assertEquals(listOf(16f), state.ticks)
    }

    @Test
    fun `drag is clamped to the valid tick range`() = runComposeUiTest {
        val state = TicksState(listOf(12f))
        setOverlayContent(state)
        // Drag the tick down past the start of the allowed range (11).
        onNodeWithTag(DIAL_GESTURE_TAG).performTouchInput {
            down(ringPointAt(12f))
            listOf(11f, 10f, 8f).forEach { moveTo(ringPointAt(it)) }
            up()
        }
        assertEquals(listOf(11f), state.ticks)
    }

    @Test
    fun `dragging does nothing while disabled`() = runComposeUiTest {
        val state = TicksState(listOf(14f))
        setOverlayContent(state, enabled = false)
        onNodeWithTag(DIAL_GESTURE_TAG).performTouchInput {
            down(ringPointAt(14f))
            listOf(15f, 16f).forEach { moveTo(ringPointAt(it)) }
            up()
        }
        assertEquals(listOf(14f), state.ticks)
    }

    @Test
    fun `a drag starting away from any tick grabs nothing`() = runComposeUiTest {
        val state = TicksState(listOf(12f))
        setOverlayContent(state)
        // Start on the ring but far (in seconds) from the tick at 12.
        onNodeWithTag(DIAL_GESTURE_TAG).performTouchInput {
            down(ringPointAt(17f))
            listOf(16f, 15f).forEach { moveTo(ringPointAt(it)) }
            up()
        }
        assertEquals(listOf(12f), state.ticks)
    }
}

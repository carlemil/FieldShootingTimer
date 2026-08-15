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
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Drives [DialGestureOverlay] directly (no ViewModel flows — see the note in
 * [DialTicksDragTest]). Dial matches the default plan: total 24s, Fire (green)
 * spans 10..15.
 */
@OptIn(ExperimentalTestApi::class)
class DialPinchTest {

    private val gap = 30f
    private val total = 24f

    // A point on the dial ring at the angle where [tickValue]'s marker sits.
    private fun TouchInjectionScope.ringPointAt(tickValue: Float): Offset {
        val ringRadius = (width / 2f) * 0.85f
        return polarToCartesian(center, ringRadius, DialGeometry.tickAngle(tickValue, total, gap))
    }

    private class PinchState {
        var duration by mutableStateOf<Float?>(null)
        var ticks by mutableStateOf(listOf<Float>())
        var rounds = 0
    }

    private fun androidx.compose.ui.test.ComposeUiTest.setOverlayContent(
        state: PinchState,
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
                        onDragSetTicks = { state.ticks = it },
                        onDragRoundTicks = { state.rounds++ },
                        onPinchSetShootingDuration = { state.duration = it }
                    )
                }
            }
        }
    }

    @Test
    fun `pinching the fire segment apart lengthens the shooting time`() = runComposeUiTest {
        val state = PinchState()
        setOverlayContent(state)
        onNodeWithTag(DIAL_GESTURE_TAG).performTouchInput {
            // Both fingers on the green arc, 3s apart; spread to 6s apart.
            down(0, ringPointAt(11f))
            down(1, ringPointAt(14f))
            moveTo(0, ringPointAt(10.5f))
            moveTo(1, ringPointAt(15.5f))
            moveTo(0, ringPointAt(10f))
            moveTo(1, ringPointAt(16f))
            up(0)
            up(1)
        }
        assertEquals(8f, state.duration)
    }

    @Test
    fun `pinching the fire segment together shortens the shooting time`() = runComposeUiTest {
        val state = PinchState()
        setOverlayContent(state)
        onNodeWithTag(DIAL_GESTURE_TAG).performTouchInput {
            // Fingers 4s apart squeeze to 2s apart: 5s Fire becomes 3s.
            down(0, ringPointAt(10.5f))
            down(1, ringPointAt(14.5f))
            moveTo(0, ringPointAt(11.5f))
            moveTo(1, ringPointAt(13.5f))
            up(0)
            up(1)
        }
        assertEquals(3f, state.duration)
    }

    @Test
    fun `pinch is clamped to the minimum shooting time`() = runComposeUiTest {
        val state = PinchState()
        setOverlayContent(state)
        onNodeWithTag(DIAL_GESTURE_TAG).performTouchInput {
            // Squeeze 5s of span down to nothing: clamps at SHOOT_TIME_MIN.
            down(0, ringPointAt(10f))
            down(1, ringPointAt(15f))
            moveTo(0, ringPointAt(12.4f))
            moveTo(1, ringPointAt(12.6f))
            up(0)
            up(1)
        }
        assertEquals(SHOOT_TIME_MIN.toFloat(), state.duration)
    }

    @Test
    fun `pinching outside the fire segment does nothing`() = runComposeUiTest {
        val state = PinchState()
        setOverlayContent(state)
        onNodeWithTag(DIAL_GESTURE_TAG).performTouchInput {
            // Both fingers on the first (TenSecondsLeft) segment's arc.
            down(0, ringPointAt(2f))
            down(1, ringPointAt(5f))
            moveTo(0, ringPointAt(1f))
            moveTo(1, ringPointAt(6f))
            up(0)
            up(1)
        }
        assertNull(state.duration)
    }

    @Test
    fun `pinch does nothing while disabled`() = runComposeUiTest {
        val state = PinchState()
        setOverlayContent(state, enabled = false)
        onNodeWithTag(DIAL_GESTURE_TAG).performTouchInput {
            down(0, ringPointAt(11f))
            down(1, ringPointAt(14f))
            moveTo(0, ringPointAt(10f))
            moveTo(1, ringPointAt(16f))
            up(0)
            up(1)
        }
        assertNull(state.duration)
    }

    @Test
    fun `a second finger on the fire segment turns a tick drag into a pinch`() = runComposeUiTest {
        val state = PinchState()
        state.ticks = listOf(14f)
        setOverlayContent(state)
        onNodeWithTag(DIAL_GESTURE_TAG).performTouchInput {
            // First finger lands on the tick at 14, second joins on the green
            // arc; spreading adjusts the duration instead of moving the tick.
            down(0, ringPointAt(14f))
            down(1, ringPointAt(11f))
            moveTo(0, ringPointAt(15f))
            moveTo(1, ringPointAt(10f))
            up(0)
            up(1)
        }
        assertEquals(7f, state.duration)
        assertEquals(listOf(14f), state.ticks)
    }
}

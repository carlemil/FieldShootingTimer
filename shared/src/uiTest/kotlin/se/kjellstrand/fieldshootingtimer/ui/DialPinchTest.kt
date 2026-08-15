package se.kjellstrand.fieldshootingtimer.ui

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

@OptIn(ExperimentalTestApi::class)
class DialPinchTest {

    // Default 5s Fire: total 24s, Fire (green) spans seconds 10..15 on the dial.
    private val segmentDurations = listOf(7f, 3f, 5f, 3f, 4f, 2f)
    private val gap = 30f
    private val total = 24f

    // A point on the dial ring at the angle where [tickValue]'s marker sits,
    // in the 24s-total scale captured when the gesture starts.
    private fun TouchInjectionScope.ringPointAt(tickValue: Float): Offset {
        val ringRadius = (width / 2f) * 0.85f
        return polarToCartesian(center, ringRadius, DialGeometry.tickAngle(tickValue, total, gap))
    }

    @Test
    fun `pinching the fire segment apart lengthens the shooting time`() = runComposeUiTest {
        val vm = TimerViewModel()
        setContent {
            FieldShootingTimerTheme(dynamicColor = false) {
                ShootTimer(vm, segmentDurations, timerSize = 300.dp)
            }
        }
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
        assertEquals(8f, vm.uiStateFlow.value.shootingDuration)
    }

    @Test
    fun `pinching the fire segment together shortens the shooting time`() = runComposeUiTest {
        val vm = TimerViewModel()
        setContent {
            FieldShootingTimerTheme(dynamicColor = false) {
                ShootTimer(vm, segmentDurations, timerSize = 300.dp)
            }
        }
        onNodeWithTag(DIAL_GESTURE_TAG).performTouchInput {
            // Fingers 4s apart squeeze to 2s apart: 5s Fire becomes 3s.
            down(0, ringPointAt(10.5f))
            down(1, ringPointAt(14.5f))
            moveTo(0, ringPointAt(11.5f))
            moveTo(1, ringPointAt(13.5f))
            up(0)
            up(1)
        }
        assertEquals(3f, vm.uiStateFlow.value.shootingDuration)
    }

    @Test
    fun `pinch is clamped to the minimum shooting time`() = runComposeUiTest {
        val vm = TimerViewModel()
        setContent {
            FieldShootingTimerTheme(dynamicColor = false) {
                ShootTimer(vm, segmentDurations, timerSize = 300.dp)
            }
        }
        onNodeWithTag(DIAL_GESTURE_TAG).performTouchInput {
            // Squeeze 5s of span down to nothing: clamps at SHOOT_TIME_MIN.
            down(0, ringPointAt(10f))
            down(1, ringPointAt(15f))
            moveTo(0, ringPointAt(12.4f))
            moveTo(1, ringPointAt(12.6f))
            up(0)
            up(1)
        }
        assertEquals(SHOOT_TIME_MIN.toFloat(), vm.uiStateFlow.value.shootingDuration)
    }

    @Test
    fun `pinching outside the fire segment does nothing`() = runComposeUiTest {
        val vm = TimerViewModel()
        setContent {
            FieldShootingTimerTheme(dynamicColor = false) {
                ShootTimer(vm, segmentDurations, timerSize = 300.dp)
            }
        }
        onNodeWithTag(DIAL_GESTURE_TAG).performTouchInput {
            // Both fingers on the first (TenSecondsLeft) segment's arc.
            down(0, ringPointAt(2f))
            down(1, ringPointAt(5f))
            moveTo(0, ringPointAt(1f))
            moveTo(1, ringPointAt(6f))
            up(0)
            up(1)
        }
        assertEquals(5f, vm.uiStateFlow.value.shootingDuration)
    }

    @Test
    fun `pinch does nothing while the timer is running`() = runComposeUiTest {
        val vm = TimerViewModel()
        vm.setTimerState(TimerRunningState.Running)
        setContent {
            FieldShootingTimerTheme(dynamicColor = false) {
                ShootTimer(vm, segmentDurations, timerSize = 300.dp)
            }
        }
        onNodeWithTag(DIAL_GESTURE_TAG).performTouchInput {
            down(0, ringPointAt(11f))
            down(1, ringPointAt(14f))
            moveTo(0, ringPointAt(10f))
            moveTo(1, ringPointAt(16f))
            up(0)
            up(1)
        }
        assertEquals(5f, vm.uiStateFlow.value.shootingDuration)
    }

    @Test
    fun `a second finger on the fire segment turns a tick drag into a pinch`() = runComposeUiTest {
        val vm = TimerViewModel()
        vm.setThumbValues(listOf(14f))
        setContent {
            FieldShootingTimerTheme(dynamicColor = false) {
                ShootTimer(vm, segmentDurations, timerSize = 300.dp)
            }
        }
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
        assertEquals(7f, vm.uiStateFlow.value.shootingDuration)
        assertEquals(listOf(14f), vm.uiStateFlow.value.thumbValues)
    }
}

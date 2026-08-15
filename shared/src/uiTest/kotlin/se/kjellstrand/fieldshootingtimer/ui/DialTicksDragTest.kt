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
class DialTicksDragTest {

    private val segmentDurations = listOf(7f, 3f, 5f, 3f, 4f, 2f) // total 24s, range 11..17
    private val gap = 30f
    private val total = 24f

    // A point on the dial ring at the angle where [tickValue]'s marker sits.
    private fun TouchInjectionScope.ringPointAt(tickValue: Float): Offset {
        val ringRadius = (width / 2f) * 0.85f
        return polarToCartesian(center, ringRadius, DialGeometry.tickAngle(tickValue, total, gap))
    }

    @Test
    fun `dragging a dial tick moves it and rounds on release`() = runComposeUiTest {
        val vm = TimerViewModel()
        vm.setThumbValues(listOf(14f))
        setContent {
            FieldShootingTimerTheme(dynamicColor = false) {
                ShootTimer(vm, segmentDurations, timerSize = 300.dp)
            }
        }
        onNodeWithTag(DIAL_GESTURE_TAG).performTouchInput {
            down(ringPointAt(14f))
            listOf(14.5f, 15f, 15.5f, 16f).forEach { moveTo(ringPointAt(it)) }
            up()
        }
        assertEquals(listOf(16f), vm.uiStateFlow.value.thumbValues)
    }

    @Test
    fun `drag is clamped to the valid tick range`() = runComposeUiTest {
        val vm = TimerViewModel()
        vm.setThumbValues(listOf(12f))
        setContent {
            FieldShootingTimerTheme(dynamicColor = false) {
                ShootTimer(vm, segmentDurations, timerSize = 300.dp)
            }
        }
        // Drag the tick down past the start of the allowed range (11).
        onNodeWithTag(DIAL_GESTURE_TAG).performTouchInput {
            down(ringPointAt(12f))
            listOf(11f, 10f, 8f).forEach { moveTo(ringPointAt(it)) }
            up()
        }
        assertEquals(listOf(11f), vm.uiStateFlow.value.thumbValues)
    }

    @Test
    fun `dragging does nothing while the timer is running`() = runComposeUiTest {
        val vm = TimerViewModel()
        vm.setThumbValues(listOf(14f))
        vm.setTimerState(TimerRunningState.Running)
        setContent {
            FieldShootingTimerTheme(dynamicColor = false) {
                ShootTimer(vm, segmentDurations, timerSize = 300.dp)
            }
        }
        onNodeWithTag(DIAL_GESTURE_TAG).performTouchInput {
            down(ringPointAt(14f))
            listOf(15f, 16f).forEach { moveTo(ringPointAt(it)) }
            up()
        }
        assertEquals(listOf(14f), vm.uiStateFlow.value.thumbValues)
    }

    @Test
    fun `a drag starting away from any tick grabs nothing`() = runComposeUiTest {
        val vm = TimerViewModel()
        vm.setThumbValues(listOf(12f))
        setContent {
            FieldShootingTimerTheme(dynamicColor = false) {
                ShootTimer(vm, segmentDurations, timerSize = 300.dp)
            }
        }
        // Start on the ring but far (in seconds) from the tick at 12.
        onNodeWithTag(DIAL_GESTURE_TAG).performTouchInput {
            down(ringPointAt(17f))
            listOf(16f, 15f).forEach { moveTo(ringPointAt(it)) }
            up()
        }
        assertEquals(listOf(12f), vm.uiStateFlow.value.thumbValues)
    }
}

package se.kjellstrand.fieldshootingtimer.ui

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream

@OptIn(ExperimentalCoroutinesApi::class)
class TimerViewModelTest {

    private lateinit var viewModel: TimerViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = TimerViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun captureStdout(block: () -> Unit): String {
        val original = System.out
        val buffer = ByteArrayOutputStream()
        System.setOut(PrintStream(buffer))
        try {
            block()
        } finally {
            System.setOut(original)
        }
        return buffer.toString()
    }

    // --- Fixed behavior: mutations must not print to stdout ---
    // (should FAIL before removing logStateChange, PASS after)

    @Test
    fun `setShootingTime does not print to stdout`() {
        val printed = captureStdout { viewModel.setShootingTime(7f) }
        assertEquals("", printed)
    }

    @Test
    fun `setTimerState does not print to stdout`() {
        val printed = captureStdout { viewModel.setTimerState(TimerRunningState.Running) }
        assertEquals("", printed)
    }

    @Test
    fun `setThumbValues does not print to stdout`() {
        val printed = captureStdout { viewModel.setThumbValues(listOf(5f, 7f)) }
        assertEquals("", printed)
    }

    @Test
    fun `addNewThumbValue does not print to stdout`() {
        val printed = captureStdout { viewModel.addNewThumbValue(4..12) }
        assertEquals("", printed)
    }

    // --- setShootingTime ---

    @Test
    fun `setShootingTime updates shootingDuration`() {
        viewModel.setShootingTime(7f)
        assertEquals(7f, viewModel.uiStateFlow.value.shootingDuration, 0f)
    }

    @Test
    fun `setShootingTime accepts zero`() {
        viewModel.setShootingTime(0f)
        assertEquals(0f, viewModel.uiStateFlow.value.shootingDuration, 0f)
    }

    @Test
    fun `setShootingTime throws on negative input`() {
        assertThrows(IllegalArgumentException::class.java) {
            viewModel.setShootingTime(-1f)
        }
    }

    // --- setTimerState ---

    @Test
    fun `setTimerState drives timer state transitions`() {
        assertEquals(TimerRunningState.NotStarted, viewModel.uiStateFlow.value.timerRunningState)

        viewModel.setTimerState(TimerRunningState.Running)
        assertEquals(TimerRunningState.Running, viewModel.uiStateFlow.value.timerRunningState)

        viewModel.setTimerState(TimerRunningState.Stopped)
        assertEquals(TimerRunningState.Stopped, viewModel.uiStateFlow.value.timerRunningState)

        viewModel.setTimerState(TimerRunningState.Finished)
        assertEquals(TimerRunningState.Finished, viewModel.uiStateFlow.value.timerRunningState)
    }

    // --- setCurrentTime ---

    @Test
    fun `setCurrentTime updates currentTime`() {
        viewModel.setCurrentTime(12.5f)
        assertEquals(12.5f, viewModel.uiStateFlow.value.currentTime, 0f)
    }

    // --- setThumbValues ---

    @Test
    fun `setThumbValues replaces thumb list`() {
        viewModel.setThumbValues(listOf(3f, 5f, 7f))
        assertEquals(listOf(3f, 5f, 7f), viewModel.uiStateFlow.value.thumbValues)

        viewModel.setThumbValues(emptyList())
        assertEquals(emptyList<Float>(), viewModel.uiStateFlow.value.thumbValues)
    }

    // --- dropLastThumbValue ---

    @Test
    fun `dropLastThumbValue drops last element`() {
        viewModel.setThumbValues(listOf(1f, 2f, 3f))
        viewModel.dropLastThumbValue()
        assertEquals(listOf(1f, 2f), viewModel.uiStateFlow.value.thumbValues)
    }

    @Test
    fun `dropLastThumbValue is a no-op on empty list`() {
        viewModel.setThumbValues(emptyList())
        viewModel.dropLastThumbValue()
        assertEquals(emptyList<Float>(), viewModel.uiStateFlow.value.thumbValues)
    }

    // --- addNewThumbValue ---

    @Test
    fun `addNewThumbValue starts at the center of the range`() {
        // range 4..12, center = (4 + 12) / 2 = 8
        viewModel.addNewThumbValue(4..12)
        assertEquals(listOf(8f), viewModel.uiStateFlow.value.thumbValues)
    }

    @Test
    fun `addNewThumbValue finds next free spot outward when center is taken`() {
        // range 4..12, center = 8; with 8 already taken, scan (forward first)
        // distance=1 → forward=9 is free → picks 9
        viewModel.setThumbValues(listOf(8f))
        viewModel.addNewThumbValue(4..12)

        val thumbs = viewModel.uiStateFlow.value.thumbValues
        assertEquals(2, thumbs.size)
        assertTrue("expected 8f to remain", thumbs.contains(8f))
        assertTrue("expected next free spot adjacent to center", thumbs.contains(9f))
    }

    @Test
    fun `addNewThumbValue does not exceed range capacity`() {
        // range 4..6 → cap is range.last - range.first = 2
        val range = 4..6
        viewModel.addNewThumbValue(range)
        viewModel.addNewThumbValue(range)
        val sizeAtCap = viewModel.uiStateFlow.value.thumbValues.size
        assertEquals(2, sizeAtCap)

        viewModel.addNewThumbValue(range)
        assertEquals(
            "third add should be refused once at capacity",
            sizeAtCap,
            viewModel.uiStateFlow.value.thumbValues.size
        )
    }

    // --- roundThumbValues ---

    @Test
    fun `roundThumbValues rounds every thumb to nearest int`() {
        viewModel.setThumbValues(listOf(3.2f, 4.7f, 5.5f, 6.4f))
        viewModel.roundThumbValues()
        assertEquals(listOf(3f, 5f, 6f, 6f), viewModel.uiStateFlow.value.thumbValues)
    }

    @Test
    fun `roundThumbValues on empty list stays empty`() {
        viewModel.setThumbValues(emptyList())
        viewModel.roundThumbValues()
        assertEquals(emptyList<Float>(), viewModel.uiStateFlow.value.thumbValues)
    }
}

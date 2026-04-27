package se.kjellstrand.fieldshootingtimer

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import se.kjellstrand.fieldshootingtimer.ui.TimerRunningState

class MainScreenPlayActionTest {

    private var startCalls = 0
    private var stopCalls = 0
    private var resetCalls = 0

    private val onStart: () -> Unit = { startCalls++ }
    private val onStop: () -> Unit = { stopCalls++ }
    private val onReset: () -> Unit = { resetCalls++ }

    @Before
    fun setUp() {
        startCalls = 0
        stopCalls = 0
        resetCalls = 0
    }

    private fun dispatch(state: TimerRunningState) =
        dispatchPlayButtonClick(state, onStart, onStop, onReset)

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `NotStarted dispatches to onStart`() {
        dispatch(TimerRunningState.NotStarted)
        assertEquals(1, startCalls)
        assertEquals(0, stopCalls)
        assertEquals(0, resetCalls)
    }

    @Test
    fun `Running dispatches to onStop`() {
        dispatch(TimerRunningState.Running)
        assertEquals(0, startCalls)
        assertEquals(1, stopCalls)
        assertEquals(0, resetCalls)
    }

    @Test
    fun `Stopped dispatches to onReset`() {
        dispatch(TimerRunningState.Stopped)
        assertEquals(0, startCalls)
        assertEquals(0, stopCalls)
        assertEquals(1, resetCalls)
    }

    @Test
    fun `Finished dispatches to onReset`() {
        dispatch(TimerRunningState.Finished)
        assertEquals(0, startCalls)
        assertEquals(0, stopCalls)
        assertEquals(1, resetCalls)
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `counters start at zero`() {
        // Fixture sanity: @Before resets cleanly between tests.
        assertEquals(0, startCalls)
        assertEquals(0, stopCalls)
        assertEquals(0, resetCalls)
    }
}

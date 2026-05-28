package se.kjellstrand.fieldshootingtimer

import se.kjellstrand.fieldshootingtimer.ui.TimerRunningState
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class MainScreenPlayActionTest {

    private var startCalls = 0
    private var stopCalls = 0
    private var resetCalls = 0

    private val onStart: () -> Unit = { startCalls++ }
    private val onStop: () -> Unit = { stopCalls++ }
    private val onReset: () -> Unit = { resetCalls++ }

    @BeforeTest
    fun setUp() {
        startCalls = 0
        stopCalls = 0
        resetCalls = 0
    }

    private fun dispatch(state: TimerRunningState) =
        dispatchPlayButtonClick(state, onStart, onStop, onReset)

    @Test
    fun notStartedDispatchesToOnStart() {
        dispatch(TimerRunningState.NotStarted)
        assertEquals(1, startCalls)
        assertEquals(0, stopCalls)
        assertEquals(0, resetCalls)
    }

    @Test
    fun runningDispatchesToOnStop() {
        dispatch(TimerRunningState.Running)
        assertEquals(0, startCalls)
        assertEquals(1, stopCalls)
        assertEquals(0, resetCalls)
    }

    @Test
    fun stoppedDispatchesToOnReset() {
        dispatch(TimerRunningState.Stopped)
        assertEquals(0, startCalls)
        assertEquals(0, stopCalls)
        assertEquals(1, resetCalls)
    }

    @Test
    fun finishedDispatchesToOnReset() {
        dispatch(TimerRunningState.Finished)
        assertEquals(0, startCalls)
        assertEquals(0, stopCalls)
        assertEquals(1, resetCalls)
    }

    @Test
    fun countersStartAtZero() {
        assertEquals(0, startCalls)
        assertEquals(0, stopCalls)
        assertEquals(0, resetCalls)
    }
}

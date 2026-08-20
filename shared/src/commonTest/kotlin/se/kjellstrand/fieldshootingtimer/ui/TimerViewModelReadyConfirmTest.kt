package se.kjellstrand.fieldshootingtimer.ui

import se.kjellstrand.fieldshootingtimer.domain.Command

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import se.kjellstrand.fieldshootingtimer.domain.TimerMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class TimerViewModelReadyConfirmTest {

    private fun kotlinx.coroutines.test.TestScope.competitionVm(): TimerViewModel =
        TimerViewModel(
            externalScope = backgroundScope,
            tickMs = 10L,
            timeSourceMs = { testScheduler.currentTime }
        ).also {
            it.setTimerMode(TimerMode.Competition)
            it.setShootingTime(5f)
        }

    @Test
    fun `competition countdown parks at zero and asks for confirmation`() = runTest {
        val vm = competitionVm()
        vm.seekTo(Command.AllReady) // park at -10
        runCurrent()
        vm.start()
        advanceTimeBy(11_000)
        runCurrent()

        assertEquals(0f, vm.uiStateFlow.value.currentTime)
        assertEquals(TimerRunningState.Stopped, vm.uiStateFlow.value.timerRunningState)
        assertTrue(vm.uiStateFlow.value.awaitingReadyConfirmation)
    }

    @Test
    fun `only the AllReady call fires while waiting - the zero cue fires on continue`() = runTest {
        val vm = competitionVm()
        val cues = mutableListOf<Command>()
        val job = backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) {
            vm.cueEventsFlow.collect { cues += it }
        }
        runCurrent()

        vm.seekTo(Command.AllReady)
        runCurrent()
        vm.start() // resuming from the AllReady row calls "Alla klara!"
        advanceTimeBy(11_000)
        runCurrent()
        assertEquals(
            listOf(Command.AllReady), cues,
            "no timed cue may fire before the dialog is answered"
        )

        vm.confirmAllReady()
        runCurrent()
        assertEquals(listOf(Command.AllReady, Command.TenSecondsLeft), cues)
        assertFalse(vm.uiStateFlow.value.awaitingReadyConfirmation)
        assertEquals(TimerRunningState.Running, vm.uiStateFlow.value.timerRunningState)
        job.cancel()
    }

    @Test
    fun `ask again calls AllReady, waits fifteen seconds, and never asks twice`() = runTest {
        val vm = competitionVm()
        val cues = mutableListOf<Command>()
        val job = backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) {
            vm.cueEventsFlow.collect { cues += it }
        }
        runCurrent()

        vm.seekTo(Command.AllReady)
        runCurrent()
        vm.start()
        advanceTimeBy(11_000)
        runCurrent()
        assertTrue(vm.uiStateFlow.value.awaitingReadyConfirmation)
        assertEquals(listOf(Command.AllReady), cues)

        vm.repeatAllReady()
        runCurrent()
        assertFalse(vm.uiStateFlow.value.awaitingReadyConfirmation)
        assertEquals(TimerRunningState.Running, vm.uiStateFlow.value.timerRunningState)
        assertTrue(vm.uiStateFlow.value.allReadyRepeat)
        val t = vm.uiStateFlow.value.currentTime
        assertTrue(t <= -14.9f, "expected the repeated 15s wait, got $t")
        // "Alla klara!" is called again immediately by the button itself.
        assertEquals(listOf(Command.AllReady, Command.AllReady), cues)

        // The repeated wait rolls straight into the sequence — the question
        // is never asked twice, and the -10s cue does not refire.
        advanceTimeBy(16_000)
        runCurrent()
        job.cancel()
        assertFalse(
            vm.uiStateFlow.value.awaitingReadyConfirmation,
            "the question must not come back after the repeated wait"
        )
        assertEquals(TimerRunningState.Running, vm.uiStateFlow.value.timerRunningState)
        assertTrue(vm.uiStateFlow.value.currentTime > 0f)
        assertEquals(
            listOf(Command.AllReady, Command.AllReady, Command.TenSecondsLeft),
            cues
        )
    }

    @Test
    fun `training mode never asks`() = runTest {
        val vm = TimerViewModel(
            externalScope = backgroundScope, tickMs = 10L,
            timeSourceMs = { testScheduler.currentTime }
        )
        vm.setShootingTime(5f)
        vm.start()
        advanceTimeBy(5_000)
        runCurrent()
        assertFalse(vm.uiStateFlow.value.awaitingReadyConfirmation)
        assertEquals(TimerRunningState.Running, vm.uiStateFlow.value.timerRunningState)
    }

    @Test
    fun `a confirmed run does not ask again at later boundaries`() = runTest {
        val vm = competitionVm()
        vm.seekTo(Command.AllReady)
        runCurrent()
        vm.start()
        advanceTimeBy(11_000)
        runCurrent()
        vm.confirmAllReady()
        advanceTimeBy(30_000) // run the whole 24s sequence
        runCurrent()
        assertEquals(TimerRunningState.Finished, vm.uiStateFlow.value.timerRunningState)
        assertFalse(vm.uiStateFlow.value.awaitingReadyConfirmation)
    }

    @Test
    fun `reset clears a pending confirmation`() = runTest {
        val vm = competitionVm()
        vm.seekTo(Command.AllReady)
        runCurrent()
        vm.start()
        advanceTimeBy(11_000)
        runCurrent()
        assertTrue(vm.uiStateFlow.value.awaitingReadyConfirmation)

        vm.reset()
        runCurrent()
        assertFalse(vm.uiStateFlow.value.awaitingReadyConfirmation)
        assertEquals(TimerRunningState.NotStarted, vm.uiStateFlow.value.timerRunningState)
    }
}

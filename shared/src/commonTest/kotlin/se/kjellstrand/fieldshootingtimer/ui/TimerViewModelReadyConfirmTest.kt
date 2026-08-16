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
    fun `no cue fires while waiting - the zero cue fires on continue`() = runTest {
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
        assertTrue(cues.isEmpty(), "no cue may fire before the dialog is answered, got $cues")

        vm.confirmAllReady()
        runCurrent()
        assertEquals(listOf(Command.TenSecondsLeft), cues)
        assertFalse(vm.uiStateFlow.value.awaitingReadyConfirmation)
        assertEquals(TimerRunningState.Running, vm.uiStateFlow.value.timerRunningState)
        job.cancel()
    }

    @Test
    fun `ask again re-runs the AllReady stretch and asks once more`() = runTest {
        val vm = competitionVm()
        vm.seekTo(Command.AllReady)
        runCurrent()
        vm.start()
        advanceTimeBy(11_000)
        runCurrent()
        assertTrue(vm.uiStateFlow.value.awaitingReadyConfirmation)

        vm.repeatAllReady()
        runCurrent()
        assertFalse(vm.uiStateFlow.value.awaitingReadyConfirmation)
        assertEquals(TimerRunningState.Running, vm.uiStateFlow.value.timerRunningState)
        val t = vm.uiStateFlow.value.currentTime
        assertTrue(t <= -9.9f, "expected the countdown back at ~-10, got $t")

        advanceTimeBy(11_000)
        runCurrent()
        assertTrue(vm.uiStateFlow.value.awaitingReadyConfirmation, "the question must come back")
        assertEquals(0f, vm.uiStateFlow.value.currentTime)
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

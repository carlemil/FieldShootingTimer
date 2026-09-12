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
class TimerViewModelSeekTest {

    // Segment starts with shooting=5: TenSecondsLeft 0, Ready 7, Fire 10,
    // CeaseFire 15; total 18. Everything after is dialog-driven.

    @Test
    fun `seekTo while running pauses at the tapped command's start`() = runTest {
        val vm = TimerViewModel(externalScope = backgroundScope, tickMs = 10L, timeSourceMs = { testScheduler.currentTime })
        vm.setShootingTime(5f)

        vm.start()
        advanceTimeBy(2_000)
        runCurrent()

        vm.seekTo(Command.Fire)
        runCurrent()

        assertEquals(10f, vm.uiStateFlow.value.currentTime)
        assertEquals(TimerRunningState.NotStarted, vm.uiStateFlow.value.timerRunningState)

        advanceTimeBy(1_000)
        runCurrent()
        assertEquals(10f, vm.uiStateFlow.value.currentTime, "timer must stay parked after seekTo")
    }

    @Test
    fun `seekTo from idle parks without starting`() = runTest {
        val vm = TimerViewModel(externalScope = backgroundScope, tickMs = 10L, timeSourceMs = { testScheduler.currentTime })
        vm.setShootingTime(5f)

        vm.seekTo(Command.CeaseFire)
        runCurrent()

        assertEquals(15f, vm.uiStateFlow.value.currentTime)
        assertEquals(TimerRunningState.NotStarted, vm.uiStateFlow.value.timerRunningState)
    }

    @Test
    fun `resuming after seekTo fires the tapped command's cue but no earlier ones`() = runTest {
        val vm = TimerViewModel(externalScope = backgroundScope, tickMs = 10L, timeSourceMs = { testScheduler.currentTime })
        vm.setShootingTime(5f)

        val collected = mutableListOf<Command>()
        val job = backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) {
            vm.cueEventsFlow.collect { collected += it }
        }
        runCurrent()

        vm.seekTo(Command.CeaseFire)
        runCurrent()
        vm.start()
        runCurrent()

        assertEquals(listOf(Command.CeaseFire), collected)

        advanceTimeBy(4_000) // 15 → past 18: finished, asking "Patron ur?"
        runCurrent()
        assertEquals(listOf(Command.CeaseFire), collected)
        assertTrue(vm.uiStateFlow.value.awaitingUnloadConfirmation)
        assertEquals(TimerRunningState.Finished, vm.uiStateFlow.value.timerRunningState)

        vm.confirmUnload()
        runCurrent()
        job.cancel()

        assertEquals(listOf(Command.CeaseFire, Command.UnloadWeapon), collected)
        // Training's chain ends there.
        assertFalse(vm.uiStateFlow.value.awaitingVisitationConfirmation)
    }

    @Test
    fun `thumbs before the seek point do not refire on resume`() = runTest {
        val vm = TimerViewModel(externalScope = backgroundScope, tickMs = 10L, timeSourceMs = { testScheduler.currentTime })
        vm.setShootingTime(5f)
        vm.setThumbValues(listOf(11f, 13f))

        val collected = mutableListOf<Float>()
        val job = backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) {
            vm.thumbCrossedFlow.collect { collected += it }
        }
        runCurrent()

        vm.seekTo(Command.CeaseFire) // 15, past both thumbs
        runCurrent()
        vm.start()
        advanceTimeBy(10_000)
        runCurrent()
        job.cancel()

        // Only the boundary flag at the dial's end (18s) lies ahead of the
        // seek point; the user thumbs (11, 13) and the Fire-start boundary
        // flag (10) are behind it and must not refire.
        assertEquals(listOf(18f), collected)
    }

    @Test
    fun `thumbs after the seek point still fire on resume`() = runTest {
        val vm = TimerViewModel(externalScope = backgroundScope, tickMs = 10L, timeSourceMs = { testScheduler.currentTime })
        vm.setShootingTime(5f)
        vm.setThumbValues(listOf(11f, 13f))

        val collected = mutableListOf<Float>()
        val job = backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) {
            vm.thumbCrossedFlow.collect { collected += it }
        }
        runCurrent()

        vm.seekTo(Command.Fire) // 10, before both thumbs
        runCurrent()
        vm.start()
        advanceTimeBy(5_000)
        runCurrent()
        job.cancel()

        // The boundary flag at the Fire start (exactly the seek point) fires
        // on resume — same semantics as the tapped command's own cue.
        assertEquals(listOf(10f, 11f, 13f), collected)
    }

    @Test
    fun `seekTo Load resets the timer and asks Ladda`() = runTest {
        val vm = TimerViewModel(externalScope = backgroundScope, tickMs = 10L, timeSourceMs = { testScheduler.currentTime })
        vm.setTimerMode(TimerMode.Competition)
        vm.setShootingTime(5f)

        vm.seekTo(Command.Fire)
        vm.start()
        advanceTimeBy(2_000)
        runCurrent()

        vm.seekTo(Command.Load)
        runCurrent()

        assertEquals(0f, vm.uiStateFlow.value.currentTime)
        assertEquals(TimerRunningState.NotStarted, vm.uiStateFlow.value.timerRunningState)
        assertTrue(vm.uiStateFlow.value.awaitingLoadConfirmation)
    }

    @Test
    fun `seekTo AllReady resets the timer and asks Alla klara`() = runTest {
        val vm = TimerViewModel(externalScope = backgroundScope, tickMs = 10L, timeSourceMs = { testScheduler.currentTime })
        vm.setTimerMode(TimerMode.Competition)
        vm.setShootingTime(5f)

        vm.seekTo(Command.AllReady)
        runCurrent()

        assertEquals(0f, vm.uiStateFlow.value.currentTime)
        assertEquals(TimerRunningState.NotStarted, vm.uiStateFlow.value.timerRunningState)
        assertTrue(vm.uiStateFlow.value.awaitingReadyConfirmation)

        // Confirming runs the sequence after the gap, skipping Ladda.
        vm.confirmAllReady()
        advanceTimeBy(1_000)
        runCurrent()
        val t = vm.uiStateFlow.value.currentTime
        assertTrue(t in -2.1f..-1.9f, "expected ~-2 after 1s of the gap, got $t")
    }

    @Test
    fun `scrubTo pauses a running timer at the given time`() = runTest {
        val vm = TimerViewModel(externalScope = backgroundScope, tickMs = 10L, timeSourceMs = { testScheduler.currentTime })
        vm.setShootingTime(5f)

        vm.start()
        advanceTimeBy(2_000)
        runCurrent()

        vm.scrubTo(12.4f)
        runCurrent()

        assertEquals(12.4f, vm.uiStateFlow.value.currentTime)
        assertEquals(TimerRunningState.NotStarted, vm.uiStateFlow.value.timerRunningState)

        advanceTimeBy(1_000)
        runCurrent()
        assertEquals(12.4f, vm.uiStateFlow.value.currentTime, "timer must stay parked after scrubTo")
    }

    @Test
    fun `resuming after scrubTo fires only cues past the scrub point`() = runTest {
        val vm = TimerViewModel(externalScope = backgroundScope, tickMs = 10L, timeSourceMs = { testScheduler.currentTime })
        vm.setShootingTime(5f)

        val collected = mutableListOf<Command>()
        val job = backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) {
            vm.cueEventsFlow.collect { collected += it }
        }
        runCurrent()

        vm.scrubTo(12f) // mid-Fire: 0, 7 and 10 are behind the scrub point
        runCurrent()
        vm.start()
        advanceTimeBy(4_000) // 12 → 16, past CeaseFire (15)
        runCurrent()
        job.cancel()

        assertEquals(listOf(Command.CeaseFire), collected)
    }

    @Test
    fun `the tapped row's call waits for play`() = runTest {
        val vm = TimerViewModel(externalScope = backgroundScope, tickMs = 10L, timeSourceMs = { testScheduler.currentTime })
        vm.setShootingTime(5f)

        val collected = mutableListOf<Command>()
        val job = backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) {
            vm.cueEventsFlow.collect { collected += it }
        }
        runCurrent()

        vm.seekTo(Command.Ready) // 7s: the tap itself is silent
        runCurrent()
        assertEquals(emptyList(), collected)

        vm.start() // the call fires as play resumes from the parked spot
        runCurrent()
        assertEquals(listOf(Command.Ready), collected)

        advanceTimeBy(4_000) // 7 → 11, past Fire at 10
        runCurrent()
        job.cancel()
        assertEquals(listOf(Command.Ready, Command.Fire), collected)
    }

    @Test
    fun `in competition a park at the sequence start runs the sequence - no Ladda dialog`() = runTest {
        val vm = TimerViewModel(externalScope = backgroundScope, tickMs = 10L, timeSourceMs = { testScheduler.currentTime })
        vm.setTimerMode(TimerMode.Competition)
        vm.setShootingTime(5f)

        vm.seekTo(Command.TenSecondsLeft)
        runCurrent()
        assertEquals(0f, vm.uiStateFlow.value.currentTime)

        vm.start()
        advanceTimeBy(1_000)
        runCurrent()
        assertTrue(
            vm.uiStateFlow.value.currentTime > 0f,
            "play from the parked sequence start must run, " +
                "got ${vm.uiStateFlow.value.currentTime}"
        )
        assertFalse(vm.uiStateFlow.value.awaitingLoadConfirmation)

        // A reset restores the untouched state: play asks Ladda again.
        vm.reset()
        runCurrent()
        vm.start()
        runCurrent()
        assertTrue(vm.uiStateFlow.value.awaitingLoadConfirmation)
        assertEquals(TimerRunningState.NotStarted, vm.uiStateFlow.value.timerRunningState)
    }

    @Test
    fun `a finished competition run chains the closing dialogs`() = runTest {
        val vm = TimerViewModel(externalScope = backgroundScope, tickMs = 10L, timeSourceMs = { testScheduler.currentTime })
        vm.setTimerMode(TimerMode.Competition)
        vm.setShootingTime(5f)

        val collected = mutableListOf<Command>()
        val job = backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) {
            vm.cueEventsFlow.collect { collected += it }
        }
        runCurrent()

        vm.seekTo(Command.CeaseFire) // 15s of 18
        runCurrent()
        vm.start()
        advanceTimeBy(4_000)
        runCurrent()
        assertEquals(TimerRunningState.Finished, vm.uiStateFlow.value.timerRunningState)
        assertTrue(vm.uiStateFlow.value.awaitingUnloadConfirmation, "asks Patron ur first")

        // Each confirmation makes its call and hands over to the next question.
        vm.confirmUnload()
        runCurrent()
        assertTrue(vm.uiStateFlow.value.awaitingVisitationConfirmation)
        vm.confirmVisitation()
        runCurrent()
        assertTrue(vm.uiStateFlow.value.awaitingVisitationDoneConfirmation)
        vm.confirmVisitationDone()
        runCurrent()
        assertTrue(vm.uiStateFlow.value.awaitingMarkConfirmation)
        vm.confirmMark()
        runCurrent()
        job.cancel()
        assertEquals(
            listOf(
                Command.CeaseFire, Command.UnloadWeapon, Command.Visitation,
                Command.VisitationDone, Command.Mark
            ),
            collected
        )

        // Training's chain is "Patron ur?" only.
        vm.reset()
        vm.setTimerMode(TimerMode.Training)
        runCurrent()
        vm.seekTo(Command.UnloadWeapon) // parks at the finished end and asks
        runCurrent()
        assertEquals(18f, vm.uiStateFlow.value.currentTime)
        assertEquals(TimerRunningState.Finished, vm.uiStateFlow.value.timerRunningState)
        assertTrue(vm.uiStateFlow.value.awaitingUnloadConfirmation)
        vm.confirmUnload()
        runCurrent()
        assertFalse(vm.uiStateFlow.value.awaitingVisitationConfirmation)
        assertFalse(vm.uiStateFlow.value.awaitingVisitationDoneConfirmation)
        assertFalse(vm.uiStateFlow.value.awaitingMarkConfirmation)
    }

    @Test
    fun `seekTo VisitationDone parks at the end and asks its dialog`() = runTest {
        val vm = TimerViewModel(externalScope = backgroundScope, tickMs = 10L, timeSourceMs = { testScheduler.currentTime })
        vm.setTimerMode(TimerMode.Competition)
        vm.setShootingTime(5f)

        vm.seekTo(Command.VisitationDone)
        runCurrent()

        assertEquals(18f, vm.uiStateFlow.value.currentTime)
        assertEquals(TimerRunningState.Finished, vm.uiStateFlow.value.timerRunningState)
        assertTrue(vm.uiStateFlow.value.awaitingVisitationDoneConfirmation)
    }

    @Test
    fun `seekTo Mark jumps to the finished end`() = runTest {
        val vm = TimerViewModel(externalScope = backgroundScope, tickMs = 10L, timeSourceMs = { testScheduler.currentTime })
        vm.setShootingTime(5f)

        vm.seekTo(Command.Mark)
        runCurrent()

        assertEquals(18f, vm.uiStateFlow.value.currentTime)
        assertEquals(TimerRunningState.Finished, vm.uiStateFlow.value.timerRunningState)
    }

    @Test
    fun `seekTo Mark asks first - the call plays on confirm only`() = runTest {
        val vm = TimerViewModel(externalScope = backgroundScope, tickMs = 10L, timeSourceMs = { testScheduler.currentTime })
        vm.setShootingTime(5f)

        val collected = mutableListOf<Command>()
        val job = backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) {
            vm.cueEventsFlow.collect { collected += it }
        }
        runCurrent()

        vm.seekTo(Command.Mark)
        runCurrent()
        assertTrue(vm.uiStateFlow.value.awaitingMarkConfirmation)
        assertEquals(emptyList(), collected, "the tap itself must not call Markera")

        vm.confirmMark()
        runCurrent()
        assertEquals(listOf(Command.Mark), collected)
        assertFalse(vm.uiStateFlow.value.awaitingMarkConfirmation)

        // Closing without confirming stays silent.
        vm.seekTo(Command.Mark)
        runCurrent()
        vm.dismissMarkConfirmation()
        runCurrent()
        job.cancel()
        assertEquals(listOf(Command.Mark), collected, "Stäng must not call Markera")
        assertFalse(vm.uiStateFlow.value.awaitingMarkConfirmation)
    }
}

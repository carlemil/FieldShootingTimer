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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class TimerViewModelSeekTest {

    // Training segment starts with shooting=5: TenSecondsLeft 0, Ready 7,
    // Fire 10, CeaseFire 15, UnloadWeaponDelay 18, UnloadWeapon 21;
    // total 25 (the Visitation stretch is competition-only).

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

        advanceTimeBy(11_000) // 15 → past 25: the delay (18) and UnloadWeapon (21)
        runCurrent()
        job.cancel()

        assertEquals(
            listOf(
                Command.CeaseFire,
                Command.UnloadWeaponDelay,
                Command.UnloadWeapon
            ),
            collected
        )
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
    fun `seekTo Load resets the timer`() = runTest {
        val vm = TimerViewModel(externalScope = backgroundScope, tickMs = 10L, timeSourceMs = { testScheduler.currentTime })
        vm.setShootingTime(5f)

        vm.start()
        advanceTimeBy(2_000)
        runCurrent()

        vm.seekTo(Command.Load)
        runCurrent()

        assertEquals(0f, vm.uiStateFlow.value.currentTime)
        assertEquals(TimerRunningState.NotStarted, vm.uiStateFlow.value.timerRunningState)
    }

    @Test
    fun `seekTo AllReady parks at the final countdown stretch and resumes from there`() = runTest {
        val vm = TimerViewModel(externalScope = backgroundScope, tickMs = 10L, timeSourceMs = { testScheduler.currentTime })
        vm.setTimerMode(TimerMode.Competition)
        vm.setShootingTime(5f)

        vm.seekTo(Command.AllReady)
        runCurrent()

        assertEquals(-10f, vm.uiStateFlow.value.currentTime)
        assertEquals(TimerRunningState.NotStarted, vm.uiStateFlow.value.timerRunningState)

        // Resuming must keep the parked countdown position, not reseed -60.
        vm.start()
        advanceTimeBy(1_000)
        runCurrent()
        val t = vm.uiStateFlow.value.currentTime
        assertTrue(t in -9.1f..-8.9f, "expected ~-9 after 1s of resumed countdown, got $t")
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
    fun `seekTo Mark jumps to the finished end`() = runTest {
        val vm = TimerViewModel(externalScope = backgroundScope, tickMs = 10L, timeSourceMs = { testScheduler.currentTime })
        vm.setShootingTime(5f)

        vm.seekTo(Command.Mark)
        runCurrent()

        assertEquals(25f, vm.uiStateFlow.value.currentTime)
        assertEquals(TimerRunningState.Finished, vm.uiStateFlow.value.timerRunningState)
    }

    @Test
    fun `seekTo Mark plays the Mark call`() = runTest {
        val vm = TimerViewModel(externalScope = backgroundScope, tickMs = 10L, timeSourceMs = { testScheduler.currentTime })
        vm.setShootingTime(5f)

        val collected = mutableListOf<Command>()
        val job = backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) {
            vm.cueEventsFlow.collect { collected += it }
        }
        runCurrent()

        vm.seekTo(Command.Mark)
        runCurrent()
        job.cancel()

        assertEquals(listOf(Command.Mark), collected)
    }
}

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

    // Segment starts with shooting=5: TenSecondsLeft 0, Ready 7, Fire 10,
    // CeaseFire 15, UnloadWeapon 18, Visitation 22; total 24.

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

        advanceTimeBy(10_000) // 15 → past 24: UnloadWeapon (18) and Visitation (22)
        runCurrent()
        job.cancel()

        assertEquals(listOf(Command.CeaseFire, Command.UnloadWeapon, Command.Visitation), collected)
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

        assertTrue(collected.isEmpty(), "thumbs behind the seek point must not refire, got $collected")
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

        assertEquals(listOf(11f, 13f), collected)
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
    fun `seekTo Mark jumps to the finished end`() = runTest {
        val vm = TimerViewModel(externalScope = backgroundScope, tickMs = 10L, timeSourceMs = { testScheduler.currentTime })
        vm.setShootingTime(5f)

        vm.seekTo(Command.Mark)
        runCurrent()

        assertEquals(24f, vm.uiStateFlow.value.currentTime)
        assertEquals(TimerRunningState.Finished, vm.uiStateFlow.value.timerRunningState)
    }
}

package se.kjellstrand.fieldshootingtimer.ui

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TimerViewModelTimingTest {

    // --- Fixed behavior: timer lifecycle is driven by the ViewModel ---

    @Test
    fun `start advances currentTime over virtual time`() = runTest {
        val vm = TimerViewModel(externalScope = backgroundScope, tickMs = 10L, timeSourceMs = { testScheduler.currentTime })
        vm.setShootingTime(5f)

        vm.start()
        advanceTimeBy(500)
        runCurrent()

        val t = vm.uiStateFlow.value.currentTime
        assertTrue("currentTime should have advanced ~0.5s, got $t", t in 0.4f..0.6f)
    }

    @Test
    fun `timer transitions to Finished when currentTime reaches totalDuration`() = runTest {
        val vm = TimerViewModel(externalScope = backgroundScope, tickMs = 10L, timeSourceMs = { testScheduler.currentTime })
        vm.setShootingTime(2f)
        runCurrent() // let stateIn-backed flows observe the new shootingDuration
        val total = vm.totalDurationFlow.value
        assertTrue("totalDuration should be > 0, got $total", total > 0f)

        vm.start()
        advanceTimeBy((total * 1000).toLong() + 500)
        runCurrent()

        assertEquals(TimerRunningState.Finished, vm.uiStateFlow.value.timerRunningState)
        assertEquals(total, vm.uiStateFlow.value.currentTime, 0.1f)
    }

    @Test
    fun `cueEventsFlow emits each timed command exactly once`() = runTest {
        val vm = TimerViewModel(externalScope = backgroundScope, tickMs = 10L, timeSourceMs = { testScheduler.currentTime })
        vm.setShootingTime(2f)

        val collected = mutableListOf<Command>()
        val job = backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) {
            vm.cueEventsFlow.collect { collected += it }
        }
        runCurrent()

        vm.start()
        // total = 7 + 3 + 2 + 3 + 4 + 2 = 21 seconds
        advanceTimeBy(22_000)
        runCurrent()
        job.cancel()

        assertEquals(
            listOf(
                Command.TenSecondsLeft,
                Command.Ready,
                Command.Fire,
                Command.CeaseFire,
                Command.UnloadWeapon,
                Command.Visitation
            ),
            collected
        )
    }

    @Test
    fun `cueEventsFlow does not replay history to a late subscriber`() = runTest {
        // Regression: replay=8 caused audio cues to refire on rotation when
        // MainScreen's collector was recreated. Late subscribers must see
        // future emissions only.
        val vm = TimerViewModel(externalScope = backgroundScope, tickMs = 10L, timeSourceMs = { testScheduler.currentTime })
        vm.setShootingTime(2f)

        vm.start()
        advanceTimeBy(8_000) // past TenSecondsLeft (0s) and Ready (7s)
        runCurrent()

        val collected = mutableListOf<Command>()
        val job = backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) {
            vm.cueEventsFlow.collect { collected += it }
        }
        runCurrent()

        assertTrue(
            "late subscriber should not receive any past cues, got $collected",
            collected.isEmpty()
        )
        job.cancel()
    }

    @Test
    fun `thumbCrossedFlow emits when currentTime crosses a thumb value`() = runTest {
        val vm = TimerViewModel(externalScope = backgroundScope, tickMs = 10L, timeSourceMs = { testScheduler.currentTime })
        vm.setShootingTime(5f)
        vm.setThumbValues(listOf(11f, 13f))

        val collected = mutableListOf<Float>()
        val job = backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) {
            vm.thumbCrossedFlow.collect { collected += it }
        }

        vm.start()
        // run past t=13 (totalDuration with shooting=5 is 7+3+5+3+4+2=24)
        advanceTimeBy(14_000)
        runCurrent()
        job.cancel()

        assertEquals(listOf(11f, 13f), collected)
    }

    @Test
    fun `stop halts the timer at the current time`() = runTest {
        val vm = TimerViewModel(externalScope = backgroundScope, tickMs = 10L, timeSourceMs = { testScheduler.currentTime })
        vm.setShootingTime(5f)

        vm.start()
        advanceTimeBy(500)
        runCurrent()
        val atStop = vm.uiStateFlow.value.currentTime

        vm.stop()
        runCurrent()
        advanceTimeBy(1_000)
        runCurrent()

        assertEquals(TimerRunningState.Stopped, vm.uiStateFlow.value.timerRunningState)
        assertEquals(
            "currentTime should not advance after stop()",
            atStop, vm.uiStateFlow.value.currentTime, 0.01f
        )
    }

    @Test
    fun `reset returns currentTime to zero and state to NotStarted`() = runTest {
        val vm = TimerViewModel(externalScope = backgroundScope, tickMs = 10L, timeSourceMs = { testScheduler.currentTime })
        vm.setShootingTime(5f)

        vm.start()
        advanceTimeBy(500)
        runCurrent()
        assertNotEquals(0f, vm.uiStateFlow.value.currentTime)

        vm.reset()
        runCurrent()

        assertEquals(0f, vm.uiStateFlow.value.currentTime, 0f)
        assertEquals(TimerRunningState.NotStarted, vm.uiStateFlow.value.timerRunningState)
    }

    @Test
    fun `currentTime tracks wall clock, not tick count, when ticks are slow`() = runTest {
        // Simulates a stalled main thread: virtual scheduler ticks once, but
        // the wall-clock time source has jumped a full second. currentTime
        // must reflect the wall clock (regression: previously incremented by
        // tickMs/1000f per tick, drifting behind real time under load).
        var fakeNow = 0L
        val vm = TimerViewModel(
            externalScope = backgroundScope,
            tickMs = 10L,
            timeSourceMs = { fakeNow }
        )
        vm.setShootingTime(5f)

        vm.start()
        runCurrent()

        fakeNow = 1000L
        advanceTimeBy(10)
        runCurrent()

        assertEquals(1.0f, vm.uiStateFlow.value.currentTime, 0.05f)
    }

    // --- Guard tests: derived flows produce the expected values ---

    @Test
    fun `segmentDurationsFlow reflects the six timed commands with Fire set to shootingDuration`() = runTest {
        val vm = TimerViewModel(externalScope = backgroundScope)
        vm.setShootingTime(4f)
        runCurrent()
        // TenSecondsLeft=7, Ready=3, Fire=4 (user), CeaseFire=3, UnloadWeapon=4, Visitation=2
        assertEquals(
            listOf(7f, 3f, 4f, 3f, 4f, 2f),
            vm.segmentDurationsFlow.value
        )
    }

    @Test
    fun `totalDurationFlow equals the sum of segment durations`() = runTest {
        val vm = TimerViewModel(externalScope = backgroundScope)
        vm.setShootingTime(5f)
        runCurrent()
        // 7 + 3 + 5 + 3 + 4 + 2 = 24
        assertEquals(24f, vm.totalDurationFlow.value, 0f)
    }

    @Test
    fun `rangeFlow first entry is just after the pre-fire commands`() = runTest {
        val vm = TimerViewModel(externalScope = backgroundScope)
        vm.setShootingTime(5f)
        runCurrent()
        val range = vm.rangeFlow.value
        // TenSecondsLeft(7) + Ready(3) = 10, so first = 11
        assertEquals(11, range.first)
        // last = 5 + 10 + 3 - 1 = 17
        assertEquals(17, range.last)
    }
}

package se.kjellstrand.fieldshootingtimer.ui

import se.kjellstrand.fieldshootingtimer.domain.Command

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

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
        assertTrue(t in 0.4f..0.6f, "currentTime should have advanced ~0.5s, got $t")
    }

    @Test
    fun `timer transitions to Finished when currentTime reaches totalDuration`() = runTest {
        val vm = TimerViewModel(externalScope = backgroundScope, tickMs = 10L, timeSourceMs = { testScheduler.currentTime })
        vm.setShootingTime(2f)
        runCurrent() // let stateIn-backed flows observe the new shootingDuration
        val total = vm.segmentDurationsFlow.value.sum()
        assertTrue(total > 0f, "totalDuration should be > 0, got $total")

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
        // total = 7 + 3 + 2 + 3 + 3 + 4 + 2 + 2 = 26 seconds
        advanceTimeBy(27_000)
        runCurrent()
        job.cancel()

        assertEquals(
            listOf(
                Command.TenSecondsLeft,
                Command.Ready,
                Command.Fire,
                Command.CeaseFire,
                Command.UnloadWeaponDelay,
                Command.UnloadWeapon,
                Command.VisitationDelay,
                Command.Visitation
            ),
            collected
        )
    }

    @Test
    fun `beep event fires just before the yellow segment's end and only once`() = runTest {
        val vm = TimerViewModel(externalScope = backgroundScope, tickMs = 10L, timeSourceMs = { testScheduler.currentTime })
        vm.setShootingTime(5f)

        var beeps = 0
        val job = backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) {
            vm.beepEventsFlow.collect { beeps++ }
        }
        runCurrent()

        vm.start()
        advanceTimeBy(17_800) // yellow end is 18; beep time is 17.9
        runCurrent()
        assertEquals(0, beeps, "beep must not fire before 17.9s")

        advanceTimeBy(200)
        runCurrent()
        assertEquals(1, beeps, "beep fires at 17.9s")

        advanceTimeBy(12_000) // through the finished end
        runCurrent()
        job.cancel()
        assertEquals(1, beeps, "beep fires exactly once per run")
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
            collected.isEmpty(),
            "late subscriber should not receive any past cues, got $collected"
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
        // run past t=13 (totalDuration with shooting=5 is 29)
        advanceTimeBy(14_000)
        runCurrent()
        job.cancel()

        // The immovable boundary flag at the Fire start (10s) — shown because
        // user flags exist — vibrates like the user flags do.
        assertEquals(listOf(10f, 11f, 13f), collected)
    }

    @Test
    fun `boundary flags do not vibrate when no user thumbs exist`() = runTest {
        val vm = TimerViewModel(externalScope = backgroundScope, tickMs = 10L, timeSourceMs = { testScheduler.currentTime })
        vm.setShootingTime(5f)

        val collected = mutableListOf<Float>()
        val job = backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) {
            vm.thumbCrossedFlow.collect { collected += it }
        }

        vm.start()
        advanceTimeBy(30_000) // past the full 29s sequence
        runCurrent()
        job.cancel()

        assertTrue(collected.isEmpty(), "no flags shown, so nothing may vibrate, got $collected")
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
            atStop, vm.uiStateFlow.value.currentTime, 0.01f,
            "currentTime should not advance after stop()"
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

        assertEquals(0f, vm.uiStateFlow.value.currentTime)
        assertEquals(TimerRunningState.NotStarted, vm.uiStateFlow.value.timerRunningState)
    }

    @Test
    fun `frameTimeSeconds tracks the running clock and clears when not running`() = runTest {
        val vm = TimerViewModel(externalScope = backgroundScope, tickMs = 10L, timeSourceMs = { testScheduler.currentTime })
        vm.setShootingTime(5f)

        assertNull(vm.frameTimeSeconds(), "no anchor before start")

        vm.start()
        advanceTimeBy(500)
        runCurrent()
        val t = vm.frameTimeSeconds()
        assertTrue(t != null && t in 0.45f..0.55f, "expected ~0.5s from the frame clock, got $t")

        vm.stop()
        assertNull(vm.frameTimeSeconds(), "anchor must clear on stop")

        vm.start() // resume
        advanceTimeBy((30_000))
        runCurrent()
        assertEquals(TimerRunningState.Finished, vm.uiStateFlow.value.timerRunningState)
        assertNull(vm.frameTimeSeconds(), "anchor must clear when the run finishes")
    }

    @Test
    fun `currentTime tracks wall clock rather than tick count when ticks are slow`() = runTest {
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
    fun `segmentDurationsFlow reflects the timed commands with Fire set to shootingDuration`() = runTest {
        val vm = TimerViewModel(externalScope = backgroundScope)
        vm.setShootingTime(4f)
        runCurrent()
        // TenSecondsLeft=7, Ready=3, Fire=4 (user), CeaseFire=3,
        // UnloadWeaponDelay=3, UnloadWeapon=4, VisitationDelay=2, Visitation=2
        assertEquals(
            listOf(7f, 3f, 4f, 3f, 3f, 4f, 2f, 2f),
            vm.segmentDurationsFlow.value
        )
    }

    @Test
    fun `segment durations sum to the expected total`() = runTest {
        val vm = TimerViewModel(externalScope = backgroundScope)
        vm.setShootingTime(5f)
        runCurrent()
        // 7 + 3 + 5 + 3 + 3 + 4 + 2 + 2 = 29
        assertEquals(29f, vm.segmentDurationsFlow.value.sum())
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

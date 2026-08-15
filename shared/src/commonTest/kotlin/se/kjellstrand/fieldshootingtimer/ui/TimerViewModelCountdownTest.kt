package se.kjellstrand.fieldshootingtimer.ui

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import se.kjellstrand.fieldshootingtimer.domain.Command
import se.kjellstrand.fieldshootingtimer.domain.TimerMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Competition-mode preparation countdown, modeled as currentTime -60..0. */
@OptIn(ExperimentalCoroutinesApi::class)
class TimerViewModelCountdownTest {

    private fun kotlinx.coroutines.test.TestScope.competitionVm(): TimerViewModel {
        val vm = TimerViewModel(
            externalScope = backgroundScope,
            tickMs = 10L,
            timeSourceMs = { testScheduler.currentTime }
        )
        vm.setTimerMode(TimerMode.Competition)
        return vm
    }

    @Test
    fun `competition start seeds the countdown at minus sixty`() = runTest {
        val vm = competitionVm()
        vm.start()
        runCurrent()
        assertTrue(
            vm.uiStateFlow.value.currentTime <= -59.9f,
            "expected ~-60, got ${vm.uiStateFlow.value.currentTime}"
        )
        assertEquals(TimerRunningState.Running, vm.uiStateFlow.value.timerRunningState)
    }

    @Test
    fun `training start is unaffected by the countdown`() = runTest {
        val vm = TimerViewModel(
            externalScope = backgroundScope,
            tickMs = 10L,
            timeSourceMs = { testScheduler.currentTime }
        )
        vm.start()
        runCurrent()
        assertTrue(vm.uiStateFlow.value.currentTime >= 0f)
    }

    @Test
    fun `no cues fire during the countdown`() = runTest {
        val vm = competitionVm()
        val collected = mutableListOf<Command>()
        val job = backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) {
            vm.cueEventsFlow.collect { collected += it }
        }
        runCurrent()

        vm.start()
        advanceTimeBy(59_000) // still one second short of the sequence
        runCurrent()
        job.cancel()

        assertEquals(emptyList(), collected)
    }

    @Test
    fun `the first cue fires as the countdown crosses zero`() = runTest {
        val vm = competitionVm()
        val collected = mutableListOf<Command>()
        val job = backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) {
            vm.cueEventsFlow.collect { collected += it }
        }
        runCurrent()

        vm.start()
        advanceTimeBy(61_000)
        runCurrent()
        job.cancel()

        assertEquals(listOf(Command.TenSecondsLeft), collected)
    }

    @Test
    fun `stop mid-countdown cancels it back to NotStarted at zero`() = runTest {
        val vm = competitionVm()
        vm.start()
        advanceTimeBy(20_000)
        runCurrent()
        vm.stop()
        runCurrent()

        assertEquals(0f, vm.uiStateFlow.value.currentTime)
        assertEquals(TimerRunningState.NotStarted, vm.uiStateFlow.value.timerRunningState)

        // Starting again begins a fresh full countdown.
        vm.start()
        runCurrent()
        assertTrue(
            vm.uiStateFlow.value.currentTime <= -59.9f,
            "expected a fresh -60 countdown, got ${vm.uiStateFlow.value.currentTime}"
        )
    }

    @Test
    fun `stop after the countdown still pauses the sequence normally`() = runTest {
        val vm = competitionVm()
        vm.start()
        advanceTimeBy(65_000) // 5s into the sequence
        runCurrent()
        vm.stop()
        runCurrent()

        assertEquals(TimerRunningState.Stopped, vm.uiStateFlow.value.timerRunningState)
        assertTrue(vm.uiStateFlow.value.currentTime in 4f..6f)
    }

    @Test
    fun `reset from a countdown returns to zero and NotStarted`() = runTest {
        val vm = competitionVm()
        vm.start()
        advanceTimeBy(15_000)
        runCurrent()
        vm.reset()
        runCurrent()

        assertEquals(0f, vm.uiStateFlow.value.currentTime)
        assertEquals(TimerRunningState.NotStarted, vm.uiStateFlow.value.timerRunningState)
    }

    @Test
    fun `full competition run finishes after countdown plus sequence`() = runTest {
        val vm = competitionVm()
        vm.setShootingTime(2f)
        runCurrent()
        val total = vm.segmentDurationsFlow.value.sum() // 21s

        vm.start()
        advanceTimeBy(60_000 + (total * 1000).toLong() + 500)
        runCurrent()

        assertEquals(TimerRunningState.Finished, vm.uiStateFlow.value.timerRunningState)
        assertEquals(total, vm.uiStateFlow.value.currentTime, 0.1f)
    }

    @Test
    fun `mode persists through the settings store`() = runTest {
        val stored = mutableMapOf<String, TimerMode>()
        val store = object : se.kjellstrand.fieldshootingtimer.persistence.SettingsStore {
            override suspend fun loadShootingDuration(): Float? = null
            override suspend fun saveShootingDuration(value: Float) {}
            override suspend fun loadThumbValues(): List<Float>? = null
            override suspend fun saveThumbValues(values: List<Float>) {}
            override suspend fun loadTimerMode(): TimerMode? = stored["mode"]
            override suspend fun saveTimerMode(mode: TimerMode) {
                stored["mode"] = mode
            }
        }
        val vm = TimerViewModel(externalScope = backgroundScope, settingsStore = store)
        runCurrent()
        vm.setTimerMode(TimerMode.Competition)
        runCurrent()
        assertEquals(TimerMode.Competition, stored["mode"])

        val vm2 = TimerViewModel(externalScope = backgroundScope, settingsStore = store)
        runCurrent()
        assertEquals(TimerMode.Competition, vm2.uiStateFlow.value.timerMode)
    }
}

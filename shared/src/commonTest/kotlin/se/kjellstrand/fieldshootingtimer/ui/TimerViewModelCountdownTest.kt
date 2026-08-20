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

/**
 * Competition-mode preparation countdown, modeled as currentTime -70..0:
 * a 60s Ladda phase followed by the 10s Alla klara wait.
 */
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
    fun `competition start seeds the countdown at minus seventy`() = runTest {
        val vm = competitionVm()
        vm.start()
        runCurrent()
        assertTrue(
            vm.uiStateFlow.value.currentTime <= -69.9f,
            "expected ~-70, got ${vm.uiStateFlow.value.currentTime}"
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
    fun `the preparation calls fire during the countdown - nothing else`() = runTest {
        val vm = competitionVm()
        val collected = mutableListOf<Command>()
        val job = backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) {
            vm.cueEventsFlow.collect { collected += it }
        }
        runCurrent()

        vm.start() // "Ladda!" as the countdown starts
        runCurrent()
        assertEquals(listOf(Command.Load), collected)

        advanceTimeBy(59_000) // -11s: still only Load
        runCurrent()
        assertEquals(listOf(Command.Load), collected)

        advanceTimeBy(10_000) // past -10: "Alla klara!", but no timed cues yet
        runCurrent()
        job.cancel()

        assertEquals(listOf(Command.Load, Command.AllReady), collected)
    }

    @Test
    fun `the countdown ends in the ready question - the first cue fires on continue`() = runTest {
        val vm = competitionVm()
        val collected = mutableListOf<Command>()
        val job = backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) {
            vm.cueEventsFlow.collect { collected += it }
        }
        runCurrent()

        vm.start()
        advanceTimeBy(71_000)
        runCurrent()
        assertTrue(vm.uiStateFlow.value.awaitingReadyConfirmation)
        assertEquals(listOf(Command.Load, Command.AllReady), collected)

        vm.confirmAllReady()
        runCurrent()
        job.cancel()

        assertEquals(
            listOf(Command.Load, Command.AllReady, Command.TenSecondsLeft),
            collected
        )
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
            vm.uiStateFlow.value.currentTime <= -69.9f,
            "expected a fresh -70 countdown, got ${vm.uiStateFlow.value.currentTime}"
        )
    }

    @Test
    fun `stop after the countdown still pauses the sequence normally`() = runTest {
        val vm = competitionVm()
        vm.start()
        advanceTimeBy(71_000)
        runCurrent()
        vm.confirmAllReady() // answer the ready question, sequence runs from 0
        advanceTimeBy(5_000) // 5s into the sequence
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
        advanceTimeBy(71_000)
        runCurrent()
        vm.confirmAllReady() // answer the ready question at 0
        advanceTimeBy((total * 1000).toLong() + 500)
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
            override suspend fun loadTutorialSeen(): Boolean? = true
            override suspend fun saveTutorialSeen(seen: Boolean) {}
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

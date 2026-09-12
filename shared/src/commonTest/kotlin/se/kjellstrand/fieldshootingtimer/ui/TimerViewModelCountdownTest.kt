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
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Competition-mode preparation: play asks "Ladda?", confirming calls it and
 * asks "Alla klara?", confirming that calls it and asks "10 sekunder kvar?",
 * confirming that runs the sequence (whose first cue is that call).
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

    private fun kotlinx.coroutines.test.TestScope.collectCues(vm: TimerViewModel): MutableList<Command> {
        val collected = mutableListOf<Command>()
        backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) {
            vm.cueEventsFlow.collect { collected += it }
        }
        runCurrent()
        return collected
    }

    @Test
    fun `competition play asks Ladda instead of running`() = runTest {
        val vm = competitionVm()
        val cues = collectCues(vm)
        vm.start()
        runCurrent()
        assertTrue(vm.uiStateFlow.value.awaitingLoadConfirmation)
        assertEquals(TimerRunningState.NotStarted, vm.uiStateFlow.value.timerRunningState)
        assertEquals(0f, vm.uiStateFlow.value.currentTime)
        assertEquals(emptyList(), cues)
    }

    @Test
    fun `training start is unaffected`() = runTest {
        val vm = TimerViewModel(
            externalScope = backgroundScope,
            tickMs = 10L,
            timeSourceMs = { testScheduler.currentTime }
        )
        vm.start()
        runCurrent()
        assertFalse(vm.uiStateFlow.value.awaitingLoadConfirmation)
        assertEquals(TimerRunningState.Running, vm.uiStateFlow.value.timerRunningState)
        assertTrue(vm.uiStateFlow.value.currentTime >= 0f)
    }

    @Test
    fun `confirming Ladda calls it and asks Alla klara`() = runTest {
        val vm = competitionVm()
        val cues = collectCues(vm)
        vm.start()
        vm.confirmLoad()
        runCurrent()
        assertEquals(listOf(Command.Load), cues)
        assertFalse(vm.uiStateFlow.value.awaitingLoadConfirmation)
        assertTrue(vm.uiStateFlow.value.awaitingReadyConfirmation)
        assertEquals(TimerRunningState.NotStarted, vm.uiStateFlow.value.timerRunningState)
    }

    @Test
    fun `confirming Alla klara calls it and asks 10 sekunder kvar`() = runTest {
        val vm = competitionVm()
        val cues = collectCues(vm)
        vm.start()
        vm.confirmLoad()
        vm.confirmAllReady()
        runCurrent()
        assertFalse(vm.uiStateFlow.value.awaitingReadyConfirmation)
        assertTrue(vm.uiStateFlow.value.awaitingTenSecondsConfirmation)
        assertEquals(TimerRunningState.NotStarted, vm.uiStateFlow.value.timerRunningState)
        assertEquals(listOf(Command.Load, Command.AllReady), cues)

        advanceTimeBy(5_000) // nothing runs while the dialog is open
        runCurrent()
        assertEquals(listOf(Command.Load, Command.AllReady), cues)
        assertEquals(0f, vm.uiStateFlow.value.currentTime)
    }

    @Test
    fun `confirming 10 sekunder kvar runs the sequence with the call as its first cue`() = runTest {
        val vm = competitionVm()
        val cues = collectCues(vm)
        vm.start()
        vm.confirmLoad()
        vm.confirmAllReady()
        vm.confirmTenSeconds()
        runCurrent()
        assertFalse(vm.uiStateFlow.value.awaitingTenSecondsConfirmation)
        assertEquals(TimerRunningState.Running, vm.uiStateFlow.value.timerRunningState)
        assertEquals(listOf(Command.Load, Command.AllReady, Command.TenSecondsLeft), cues)

        advanceTimeBy(7_500) // past Ready at 7
        runCurrent()
        assertEquals(
            listOf(Command.Load, Command.AllReady, Command.TenSecondsLeft, Command.Ready),
            cues
        )
        assertTrue(vm.uiStateFlow.value.currentTime > 7f)
    }

    @Test
    fun `confirmations are ignored when their dialog is not open`() = runTest {
        val vm = competitionVm()
        val cues = collectCues(vm)
        vm.confirmLoad()
        vm.confirmAllReady()
        vm.confirmTenSeconds()
        runCurrent()
        assertEquals(emptyList(), cues)
        assertEquals(TimerRunningState.NotStarted, vm.uiStateFlow.value.timerRunningState)
    }

    @Test
    fun `closing a dialog returns to rest and play asks Ladda again`() = runTest {
        val vm = competitionVm()
        vm.start()
        vm.confirmLoad()
        vm.dismissReadyConfirmation()
        runCurrent()
        assertFalse(vm.uiStateFlow.value.awaitingReadyConfirmation)
        assertEquals(TimerRunningState.NotStarted, vm.uiStateFlow.value.timerRunningState)

        vm.start()
        runCurrent()
        assertTrue(vm.uiStateFlow.value.awaitingLoadConfirmation)
        vm.dismissLoadConfirmation()
        runCurrent()
        assertFalse(vm.uiStateFlow.value.awaitingLoadConfirmation)
    }

    @Test
    fun `closing 10 sekunder kvar leaves the timer at rest and play asks Ladda again`() = runTest {
        val vm = competitionVm()
        vm.start()
        vm.confirmLoad()
        vm.confirmAllReady()
        vm.dismissTenSecondsConfirmation()
        runCurrent()
        assertFalse(vm.uiStateFlow.value.awaitingTenSecondsConfirmation)
        assertEquals(0f, vm.uiStateFlow.value.currentTime)
        assertEquals(TimerRunningState.NotStarted, vm.uiStateFlow.value.timerRunningState)

        vm.start()
        runCurrent()
        assertTrue(vm.uiStateFlow.value.awaitingLoadConfirmation)
    }

    @Test
    fun `stop pauses the sequence normally`() = runTest {
        val vm = competitionVm()
        vm.start()
        vm.confirmLoad()
        vm.confirmAllReady()
        vm.confirmTenSeconds()
        advanceTimeBy(5_000) // 5s into the sequence
        runCurrent()
        vm.stop()
        runCurrent()

        assertEquals(TimerRunningState.Stopped, vm.uiStateFlow.value.timerRunningState)
        assertTrue(vm.uiStateFlow.value.currentTime in 4f..6f)
    }

    @Test
    fun `reset clears a pending dialog`() = runTest {
        val vm = competitionVm()
        vm.start()
        vm.confirmLoad()
        runCurrent()
        assertTrue(vm.uiStateFlow.value.awaitingReadyConfirmation)
        vm.reset()
        runCurrent()
        assertFalse(vm.uiStateFlow.value.awaitingReadyConfirmation)
        assertEquals(0f, vm.uiStateFlow.value.currentTime)
        assertEquals(TimerRunningState.NotStarted, vm.uiStateFlow.value.timerRunningState)
    }

    @Test
    fun `full competition run finishes after the dialogs plus sequence`() = runTest {
        val vm = competitionVm()
        vm.setShootingTime(2f)
        runCurrent()
        val total = vm.segmentDurationsFlow.value.sum() // 15s

        vm.start()
        vm.confirmLoad()
        vm.confirmAllReady()
        vm.confirmTenSeconds()
        advanceTimeBy((total * 1000).toLong() + 500)
        runCurrent()

        assertEquals(TimerRunningState.Finished, vm.uiStateFlow.value.timerRunningState)
        assertEquals(total, vm.uiStateFlow.value.currentTime, 0.1f)
        assertFalse(vm.uiStateFlow.value.awaitingReadyConfirmation)
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

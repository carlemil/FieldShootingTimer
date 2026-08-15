package se.kjellstrand.fieldshootingtimer.ui

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import se.kjellstrand.fieldshootingtimer.domain.TimerMode
import se.kjellstrand.fieldshootingtimer.persistence.SettingsStore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class TutorialStateTest {

    private class FakeStore : SettingsStore {
        var tutorialSeen: Boolean? = null
        override suspend fun loadShootingDuration(): Float? = null
        override suspend fun saveShootingDuration(value: Float) {}
        override suspend fun loadThumbValues(): List<Float>? = null
        override suspend fun saveThumbValues(values: List<Float>) {}
        override suspend fun loadTimerMode(): TimerMode? = null
        override suspend fun saveTimerMode(mode: TimerMode) {}
        override suspend fun loadTutorialSeen(): Boolean? = tutorialSeen
        override suspend fun saveTutorialSeen(seen: Boolean) {
            tutorialSeen = seen
        }
    }

    @Test
    fun `no store means no tutorial`() = runTest {
        val vm = TimerViewModel(externalScope = backgroundScope)
        runCurrent()
        assertTrue(vm.uiStateFlow.value.tutorialSeen)
    }

    @Test
    fun `first launch with a store shows the tutorial`() = runTest {
        val vm = TimerViewModel(externalScope = backgroundScope, settingsStore = FakeStore())
        runCurrent()
        assertFalse(vm.uiStateFlow.value.tutorialSeen)
    }

    @Test
    fun `marking the tutorial seen persists and sticks on next launch`() = runTest {
        val store = FakeStore()
        val vm = TimerViewModel(externalScope = backgroundScope, settingsStore = store)
        runCurrent()
        vm.markTutorialSeen()
        runCurrent()
        assertTrue(vm.uiStateFlow.value.tutorialSeen)
        assertEquals(true, store.tutorialSeen)

        val vm2 = TimerViewModel(externalScope = backgroundScope, settingsStore = store)
        runCurrent()
        assertTrue(vm2.uiStateFlow.value.tutorialSeen)
    }
}

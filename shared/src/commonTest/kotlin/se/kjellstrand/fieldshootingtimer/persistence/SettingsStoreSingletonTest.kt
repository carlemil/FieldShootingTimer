package se.kjellstrand.fieldshootingtimer.persistence

import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import se.kjellstrand.fieldshootingtimer.domain.TimerMode

private class FakeSettingsStore : SettingsStore {
    override suspend fun loadShootingDuration(): Float? = null
    override suspend fun saveShootingDuration(value: Float) = Unit
    override suspend fun loadThumbValues(): List<Float>? = null
    override suspend fun saveThumbValues(values: List<Float>) = Unit
    override suspend fun loadTimerMode(): TimerMode? = null
    override suspend fun saveTimerMode(mode: TimerMode) = Unit
    override suspend fun loadTutorialSeen(): Boolean? = null
    override suspend fun saveTutorialSeen(seen: Boolean) = Unit
}

class SettingsStoreSingletonTest {

    @AfterTest
    fun tearDown() = resetSettingsStoreSingleton()

    @Test
    fun secondCallReturnsFirstInstance() {
        val first = settingsStoreSingleton { FakeSettingsStore() }
        val second = settingsStoreSingleton { FakeSettingsStore() }
        assertSame(first, second)
    }

    @Test
    fun createIsOnlyInvokedOnce() {
        var creations = 0
        repeat(3) {
            settingsStoreSingleton {
                creations++
                FakeSettingsStore()
            }
        }
        assertEquals(1, creations)
    }
}

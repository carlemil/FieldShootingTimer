package se.kjellstrand.fieldshootingtimer.persistence

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import se.kjellstrand.fieldshootingtimer.domain.TimerMode

// In-memory store: host-side tests need determinism, not persistence.
private class InMemorySettingsStore : SettingsStore {
    private var shootingDuration: Float? = null
    private var thumbValues: List<Float>? = null
    private var timerMode: TimerMode? = null
    private var tutorialSeen: Boolean? = null

    override suspend fun loadShootingDuration(): Float? = shootingDuration

    override suspend fun saveShootingDuration(value: Float) {
        shootingDuration = value
    }

    override suspend fun loadThumbValues(): List<Float>? = thumbValues

    override suspend fun saveThumbValues(values: List<Float>) {
        thumbValues = values
    }

    override suspend fun loadTimerMode(): TimerMode? = timerMode

    override suspend fun saveTimerMode(mode: TimerMode) {
        timerMode = mode
    }

    override suspend fun loadTutorialSeen(): Boolean? = tutorialSeen

    override suspend fun saveTutorialSeen(seen: Boolean) {
        tutorialSeen = seen
    }
}

@Composable
actual fun rememberSettingsStore(): SettingsStore = remember { InMemorySettingsStore() }

package se.kjellstrand.fieldshootingtimer.persistence

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

// In-memory store: host-side tests need determinism, not persistence.
private class InMemorySettingsStore : SettingsStore {
    private var shootingDuration: Float? = null
    private var thumbValues: List<Float>? = null

    override suspend fun loadShootingDuration(): Float? = shootingDuration

    override suspend fun saveShootingDuration(value: Float) {
        shootingDuration = value
    }

    override suspend fun loadThumbValues(): List<Float>? = thumbValues

    override suspend fun saveThumbValues(values: List<Float>) {
        thumbValues = values
    }
}

@Composable
actual fun rememberSettingsStore(): SettingsStore = remember { InMemorySettingsStore() }

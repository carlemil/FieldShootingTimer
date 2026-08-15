package se.kjellstrand.fieldshootingtimer.persistence

import androidx.compose.runtime.Composable
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import okio.Path.Companion.toPath
import se.kjellstrand.fieldshootingtimer.domain.TimerMode

/**
 * Persists user-configurable timer settings across process restarts.
 */
interface SettingsStore {
    suspend fun loadShootingDuration(): Float?
    suspend fun saveShootingDuration(value: Float)
    suspend fun loadThumbValues(): List<Float>?
    suspend fun saveThumbValues(values: List<Float>)
    suspend fun loadTimerMode(): TimerMode?
    suspend fun saveTimerMode(mode: TimerMode)
}

private val SHOOTING_DURATION = floatPreferencesKey("shooting_duration")
private val THUMB_VALUES = stringPreferencesKey("thumb_values_csv")
private val TIMER_MODE = stringPreferencesKey("timer_mode")

class DataStoreSettingsStore(private val dataStore: DataStore<Preferences>) : SettingsStore {
    override suspend fun loadShootingDuration(): Float? =
        dataStore.data.first()[SHOOTING_DURATION]

    override suspend fun saveShootingDuration(value: Float) {
        dataStore.edit { it[SHOOTING_DURATION] = value }
    }

    override suspend fun loadThumbValues(): List<Float>? =
        dataStore.data.first()[THUMB_VALUES]
            ?.split(",")
            ?.filter { it.isNotBlank() }
            ?.mapNotNull { it.toFloatOrNull() }

    override suspend fun saveThumbValues(values: List<Float>) {
        dataStore.edit { it[THUMB_VALUES] = values.joinToString(",") }
    }

    override suspend fun loadTimerMode(): TimerMode? =
        dataStore.data.first()[TIMER_MODE]?.let { stored ->
            TimerMode.entries.find { it.name == stored }
        }

    override suspend fun saveTimerMode(mode: TimerMode) {
        dataStore.edit { it[TIMER_MODE] = mode.name }
    }
}

/**
 * Build a [DataStore] persisted at the given filesystem path. Wires up the
 * multiplatform okio-backed PreferenceDataStoreFactory.
 */
fun createPreferencesDataStore(producePath: () -> String): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(produceFile = { producePath().toPath() })

@Composable
expect fun rememberSettingsStore(): SettingsStore

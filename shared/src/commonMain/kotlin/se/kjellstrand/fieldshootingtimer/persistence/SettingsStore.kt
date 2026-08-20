package se.kjellstrand.fieldshootingtimer.persistence

import androidx.compose.runtime.Composable
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
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
    suspend fun loadTutorialSeen(): Boolean?
    suspend fun saveTutorialSeen(seen: Boolean)

    // Defaulted so fakes/stubs that don't care about theming keep compiling;
    // null means "no explicit choice — follow the system theme".
    suspend fun loadDarkTheme(): Boolean? = null
    suspend fun saveDarkTheme(dark: Boolean) {}

    // CeaseFire cue style: true = short beep at the end of the yellow
    // segment, false/null = the spoken "Eld upphör!" at its start.
    suspend fun loadCeaseFireBeep(): Boolean? = null
    suspend fun saveCeaseFireBeep(beep: Boolean) {}
}

private val SHOOTING_DURATION = floatPreferencesKey("shooting_duration")
private val THUMB_VALUES = stringPreferencesKey("thumb_values_csv")
private val TIMER_MODE = stringPreferencesKey("timer_mode")
private val TUTORIAL_SEEN = booleanPreferencesKey("tutorial_seen")
private val DARK_THEME = booleanPreferencesKey("dark_theme")
private val CEASE_FIRE_BEEP = booleanPreferencesKey("cease_fire_beep")

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

    override suspend fun loadTutorialSeen(): Boolean? =
        dataStore.data.first()[TUTORIAL_SEEN]

    override suspend fun saveTutorialSeen(seen: Boolean) {
        dataStore.edit { it[TUTORIAL_SEEN] = seen }
    }

    override suspend fun loadDarkTheme(): Boolean? =
        dataStore.data.first()[DARK_THEME]

    override suspend fun saveDarkTheme(dark: Boolean) {
        dataStore.edit { it[DARK_THEME] = dark }
    }

    override suspend fun loadCeaseFireBeep(): Boolean? =
        dataStore.data.first()[CEASE_FIRE_BEEP]

    override suspend fun saveCeaseFireBeep(beep: Boolean) {
        dataStore.edit { it[CEASE_FIRE_BEEP] = beep }
    }
}

/**
 * Build a [DataStore] persisted at the given filesystem path. Wires up the
 * multiplatform okio-backed PreferenceDataStoreFactory.
 */
fun createPreferencesDataStore(producePath: () -> String): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(produceFile = { producePath().toPath() })

private var settingsStoreInstance: SettingsStore? = null

/**
 * Returns the process-wide [SettingsStore], creating it on the first call.
 * DataStore forbids two active instances on the same file, so the store must
 * outlive any single composition — recreating it per Activity/composition
 * crashes with IllegalStateException on the next read. Only called from
 * composition (main thread), so no locking is needed.
 */
fun settingsStoreSingleton(create: () -> SettingsStore): SettingsStore =
    settingsStoreInstance ?: create().also { settingsStoreInstance = it }

internal fun resetSettingsStoreSingleton() {
    settingsStoreInstance = null
}

@Composable
expect fun rememberSettingsStore(): SettingsStore

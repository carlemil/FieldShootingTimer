package se.kjellstrand.fieldshootingtimer.persistence

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

private const val SETTINGS_FILE = "settings.preferences_pb"

@Composable
actual fun rememberSettingsStore(): SettingsStore {
    val context = LocalContext.current.applicationContext
    return remember(context) {
        val dataStore = createPreferencesDataStore {
            context.filesDir.resolve(SETTINGS_FILE).absolutePath
        }
        DataStoreSettingsStore(dataStore)
    }
}

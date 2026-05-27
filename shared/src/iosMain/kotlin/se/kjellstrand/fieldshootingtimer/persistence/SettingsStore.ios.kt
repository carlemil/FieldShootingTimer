package se.kjellstrand.fieldshootingtimer.persistence

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

private const val SETTINGS_FILE = "settings.preferences_pb"

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun rememberSettingsStore(): SettingsStore = remember {
    val documents = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = true,
        error = null
    )
    val dataStore = createPreferencesDataStore {
        requireNotNull(documents?.path) { "Documents directory not available" } + "/" + SETTINGS_FILE
    }
    DataStoreSettingsStore(dataStore)
}

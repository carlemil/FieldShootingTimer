package se.kjellstrand.fieldshootingtimer.platform

import androidx.compose.runtime.Composable

/**
 * Opens a URL in the platform's default browser.
 * Android uses an `ACTION_VIEW` intent; iOS uses `UIApplication.openURL`.
 */
interface UrlOpener {
    fun openUrl(url: String)
}

@Composable
expect fun rememberUrlOpener(): UrlOpener

package se.kjellstrand.fieldshootingtimer.platform

import androidx.compose.runtime.Composable

/**
 * Opens the platform share sheet with a plain-text payload.
 * Android uses an `ACTION_SEND` chooser; iOS uses `UIActivityViewController`.
 */
interface Sharer {
    fun share(text: String)
}

@Composable
expect fun rememberSharer(): Sharer

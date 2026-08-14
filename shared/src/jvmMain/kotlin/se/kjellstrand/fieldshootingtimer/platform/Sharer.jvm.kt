package se.kjellstrand.fieldshootingtimer.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

private class NoOpSharer : Sharer {
    override fun share(text: String) = Unit
}

@Composable
actual fun rememberSharer(): Sharer = remember { NoOpSharer() }

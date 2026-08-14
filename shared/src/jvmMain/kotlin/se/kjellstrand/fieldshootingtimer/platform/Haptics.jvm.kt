package se.kjellstrand.fieldshootingtimer.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

private class NoOpHaptics : Haptics {
    override fun shortTick() = Unit
}

@Composable
actual fun rememberHaptics(): Haptics = remember { NoOpHaptics() }

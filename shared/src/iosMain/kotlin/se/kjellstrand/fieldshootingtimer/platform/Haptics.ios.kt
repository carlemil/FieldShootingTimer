package se.kjellstrand.fieldshootingtimer.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

// Stub; real UIImpactFeedbackGenerator-backed version lands in ios/platform-actuals.
private object IosHapticsStub : Haptics {
    override fun shortTick() {}
}

@Composable
actual fun rememberHaptics(): Haptics = remember { IosHapticsStub }

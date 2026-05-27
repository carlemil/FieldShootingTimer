package se.kjellstrand.fieldshootingtimer.platform

import androidx.compose.runtime.Composable

// Stub; real UIApplication.idleTimerDisabled binding lands in ios/platform-actuals.
@Composable
actual fun KeepScreenOn(enabled: Boolean) {
    // no-op for now
}

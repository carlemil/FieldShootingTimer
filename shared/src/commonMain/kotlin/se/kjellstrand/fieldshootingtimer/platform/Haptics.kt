package se.kjellstrand.fieldshootingtimer.platform

import androidx.compose.runtime.Composable

/**
 * Fires a single short haptic tap when the timer crosses a user-placed thumb.
 * Android uses Vibrator/VibrationEffect; iOS uses UIImpactFeedbackGenerator.
 */
interface Haptics {
    fun shortTick()
}

@Composable
expect fun rememberHaptics(): Haptics

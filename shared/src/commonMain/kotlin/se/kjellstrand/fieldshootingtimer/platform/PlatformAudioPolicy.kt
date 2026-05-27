package se.kjellstrand.fieldshootingtimer.platform

import androidx.compose.runtime.Composable

/**
 * Gates whether audio cues and haptic feedback should fire based on the
 * device's current "is the user OK with sound?" signal. On Android this
 * reads ringer mode; on iOS audio respects the silent switch automatically
 * via AVAudioSession `.ambient`, so the iOS implementation always returns
 * true and lets the OS enforce silence.
 */
interface PlatformAudioPolicy {
    fun shouldPlayCue(): Boolean
    fun shouldVibrate(): Boolean
}

@Composable
expect fun rememberPlatformAudioPolicy(): PlatformAudioPolicy

package se.kjellstrand.fieldshootingtimer.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

// iOS doesn't expose the ringer-switch state, and audio respects the silent
// switch automatically via AVAudioSession `.ambient` (configured in
// iosApp/iosAppApp.swift). Haptics fire regardless — matches Apple's Timer app.
private object IosPlatformAudioPolicy : PlatformAudioPolicy {
    override fun shouldPlayCue(): Boolean = true
    override fun shouldVibrate(): Boolean = true
}

@Composable
actual fun rememberPlatformAudioPolicy(): PlatformAudioPolicy =
    remember { IosPlatformAudioPolicy }

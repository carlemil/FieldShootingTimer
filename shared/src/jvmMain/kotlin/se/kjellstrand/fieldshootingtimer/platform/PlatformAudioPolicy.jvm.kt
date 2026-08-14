package se.kjellstrand.fieldshootingtimer.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

private class AlwaysOnAudioPolicy : PlatformAudioPolicy {
    override fun shouldPlayCue(): Boolean = true
    override fun shouldVibrate(): Boolean = true
}

@Composable
actual fun rememberPlatformAudioPolicy(): PlatformAudioPolicy =
    remember { AlwaysOnAudioPolicy() }

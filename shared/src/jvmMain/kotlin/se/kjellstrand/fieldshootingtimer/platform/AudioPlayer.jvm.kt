package se.kjellstrand.fieldshootingtimer.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import se.kjellstrand.fieldshootingtimer.domain.Command

// The jvm target exists only for host-side tests; no audio is played.
private class NoOpAudioPlayer : AudioPlayer {
    override suspend fun preload(cues: List<Command>) = Unit
    override fun play(command: Command) = Unit
    override fun playBeep() = Unit
    override fun release() = Unit
}

@Composable
actual fun rememberAudioPlayer(): AudioPlayer = remember { NoOpAudioPlayer() }

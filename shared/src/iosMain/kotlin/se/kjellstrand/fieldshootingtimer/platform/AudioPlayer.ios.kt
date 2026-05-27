package se.kjellstrand.fieldshootingtimer.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import se.kjellstrand.fieldshootingtimer.ui.Command

// Stub implementation; real AVAudioPlayer-backed version lands in
// task ios/platform-actuals.
private object IosAudioPlayerStub : AudioPlayer {
    override suspend fun preload(cues: List<Command>) {}
    override fun play(command: Command) {}
    override fun release() {}
}

@Composable
actual fun rememberAudioPlayer(): AudioPlayer = remember { IosAudioPlayerStub }

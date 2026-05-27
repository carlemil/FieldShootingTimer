package se.kjellstrand.fieldshootingtimer.platform

import androidx.compose.runtime.Composable
import se.kjellstrand.fieldshootingtimer.ui.Command

/**
 * Plays the project's audio cues (Swedish voice clips for each Command).
 * Implementations preload the audio at start-up and play with low latency.
 *
 * Acquired in Compose via [rememberAudioPlayer]; the underlying platform
 * factory binds the right runtime context (SoundPool + Android Context on
 * Android, AVAudioPlayer pool on iOS).
 */
interface AudioPlayer {
    suspend fun preload(cues: List<Command>)
    fun play(command: Command)
    fun release()
}

@Composable
expect fun rememberAudioPlayer(): AudioPlayer

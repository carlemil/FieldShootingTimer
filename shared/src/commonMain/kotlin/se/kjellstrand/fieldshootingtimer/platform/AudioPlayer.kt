package se.kjellstrand.fieldshootingtimer.platform

import androidx.compose.runtime.Composable
import se.kjellstrand.fieldshootingtimer.domain.Command

/**
 * Plays the project's audio cues (Swedish voice clips for each Command).
 * Implementations preload the audio at start-up and play with low latency.
 *
 * Acquired in Compose via [rememberAudioPlayer]; the underlying platform
 * factory binds the right runtime context (SoundPool + Android Context on
 * Android, AVAudioPlayer pool on iOS).
 */
/** The short cease-fire signal used instead of the voice when beep mode is on. */
internal const val BEEP_AUDIO_PATH = "files/beep.wav"

interface AudioPlayer {
    suspend fun preload(cues: List<Command>)
    fun play(command: Command)
    /** The loud, short cease-fire signal ([BEEP_AUDIO_PATH]). */
    fun playBeep()
    fun release()
}

@Composable
expect fun rememberAudioPlayer(): AudioPlayer

package se.kjellstrand.fieldshootingtimer.platform

import android.content.Context
import android.media.SoundPool
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import se.kjellstrand.fieldshootingtimer.ui.Command

private const val TAG = "AudioPlayer"

class AndroidAudioPlayer(private val context: Context) : AudioPlayer {
    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(5)
        .build()
    private val soundMap: MutableMap<Command, Int> = mutableMapOf()

    override suspend fun preload(cues: List<Command>) {
        cues.forEach { cue ->
            // TODO: load audio bytes via Res.readBytes(cue.audioPath) once
            // shared/compose-resources wires the asset pipeline. Until then
            // audioResId is the legacy R.raw.* int (currently 0 / placeholder
            // from the Command move) so we skip loading.
            if (cue.audioResId > 0) {
                soundMap[cue] = soundPool.load(context, cue.audioResId, 1)
            }
        }
    }

    override fun play(command: Command) {
        val soundId = soundMap[command]
        if (soundId != null) {
            soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
        } else {
            Log.d(TAG, "Sound not loaded for cue: $command (resources not yet wired)")
        }
    }

    override fun release() {
        soundPool.release()
    }
}

@Composable
actual fun rememberAudioPlayer(): AudioPlayer {
    val context = LocalContext.current
    val player = remember(context) { AndroidAudioPlayer(context.applicationContext) }
    DisposableEffect(player) { onDispose { player.release() } }
    return player
}

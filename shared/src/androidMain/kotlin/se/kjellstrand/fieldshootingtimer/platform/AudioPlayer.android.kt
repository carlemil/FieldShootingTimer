package se.kjellstrand.fieldshootingtimer.platform

import android.content.Context
import android.media.SoundPool
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import org.jetbrains.compose.resources.ExperimentalResourceApi
import se.kjellstrand.fieldshootingtimer.resources.Res
import se.kjellstrand.fieldshootingtimer.ui.Command
import java.io.File

private const val TAG = "AudioPlayer"

@OptIn(ExperimentalResourceApi::class)
class AndroidAudioPlayer(private val context: Context) : AudioPlayer {
    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(5)
        .build()
    private val soundMap: MutableMap<Command, Int> = mutableMapOf()

    override suspend fun preload(cues: List<Command>) {
        cues.forEach { cue ->
            val path = cue.audioPath ?: return@forEach
            try {
                val bytes = Res.readBytes(path)
                val cacheFile = File(context.cacheDir, path.substringAfterLast('/'))
                cacheFile.writeBytes(bytes)
                soundMap[cue] = soundPool.load(cacheFile.absolutePath, 1)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to preload $cue from $path", e)
            }
        }
    }

    override fun play(command: Command) {
        val soundId = soundMap[command]
        if (soundId != null) {
            soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
        } else {
            Log.d(TAG, "Sound not loaded for cue: $command")
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

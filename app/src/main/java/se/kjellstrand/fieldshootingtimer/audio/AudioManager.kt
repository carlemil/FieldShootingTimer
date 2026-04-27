package se.kjellstrand.fieldshootingtimer.audio

import android.content.Context
import android.media.SoundPool
import android.util.Log
import se.kjellstrand.fieldshootingtimer.ui.Command

class AudioManager(context: Context) {
    private val systemAudioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(5)
        .build()

    private val soundMap: MutableMap<Command, Int> = mutableMapOf()

    init {
        Command.audibleCommands.forEach { command ->
            val soundId = soundPool.load(context, command.audioResId, 1)
            soundMap[command] = soundId
        }
    }

    fun release() {
        soundPool.release()
    }

    fun play(command: Command) {
        val ringerMode = systemAudioManager.ringerMode
        if (!shouldPlayAudio(ringerMode)) {
            Log.d(TAG, "Skipping sound; ringer mode = $ringerMode")
            return
        }
        val soundId = soundMap[command]
        if (soundId != null) {
            soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
        } else {
            Log.e(TAG, "Sound not loaded for audio cue: $command")
        }
    }

    companion object {
        private const val TAG = "AudioManager"
    }
}

internal fun shouldPlayAudio(ringerMode: Int): Boolean =
    ringerMode == android.media.AudioManager.RINGER_MODE_NORMAL

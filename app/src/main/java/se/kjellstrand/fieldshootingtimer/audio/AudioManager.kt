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
        Command.entries.filter { it.audioResId != -1 }.forEach { command ->
            val soundId = soundPool.load(context, command.audioResId, 1)
            soundMap[command] = soundId
        }
    }

    fun release() {
        soundPool.release()
    }

    fun playAudioCue(
        audioCues: List<Pair<Float, Command>>,
        currentTime: Float,
        playedAudioIndices: Set<Int>,
        onAddPlayedAudioIndex: (index: Int) -> Unit
        ) {
        for ((index, audioCue) in audioCues.withIndex()) {
            if (currentTime >= audioCue.first &&
                !playedAudioIndices.contains(index)
            ) {
                playSound(audioCue.second)
                onAddPlayedAudioIndex(index)
            }
        }
    }

    private fun playSound(command: Command) {
        // If device is silent or vibrate, skip audio
        when (systemAudioManager.ringerMode) {
            android.media.AudioManager.RINGER_MODE_NORMAL -> {
                val soundId = soundMap[command]
                if (soundId != null) {
                    soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
                } else {
                    Log.e(TAG, "Sound not loaded for audio cue: $command")
                }
            }
            android.media.AudioManager.RINGER_MODE_SILENT -> {
                Log.d(TAG, "Skipping sound because device is in silent mode.")
                return
            }
            android.media.AudioManager.RINGER_MODE_VIBRATE -> {
                Log.d(TAG, "Skipping sound because device is in vibrate mode.")
                return
            }
        }
    }

    companion object {
        private const val TAG = "AudioManager"
    }
}

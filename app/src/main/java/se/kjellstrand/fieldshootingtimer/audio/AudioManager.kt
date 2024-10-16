package se.kjellstrand.fieldshootingtimer.audio

import android.content.Context
import android.media.SoundPool
import android.util.Log

class AudioManager(context: Context) {
    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(5)
        .build()

    private val soundMap: MutableMap<AudioCueType, Int> = mutableMapOf()

    init {
        AudioCueType.values().forEach { audioCueType ->
            val soundId = soundPool.load(context, audioCueType.resId, 1)
            soundMap[audioCueType] = soundId
        }
    }

    fun playSound(audioCueType: AudioCueType) {
        val soundId = soundMap[audioCueType]
        if (soundId != null) {
            soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
        } else {
            Log.e(TAG, "Sound not loaded for audio cue: $audioCueType")
        }
    }

    fun release() {
        soundPool.release()
    }

    companion object {
        private const val TAG = "AudioManager"
    }
}

package se.kjellstrand.fieldshootingtimer.audio

import android.content.Context
import android.media.SoundPool
import android.util.Log
import se.kjellstrand.fieldshootingtimer.ui.TimerUiState

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

    fun release() {
        soundPool.release()
    }

    fun playAudioCue(
        audioCues: List<AudioCue>,
        timerUiState: TimerUiState,
        playedAudioIndices: MutableSet<Int>
    ) {
        for ((index, audioCue) in audioCues.withIndex()) {
            if (timerUiState.currentTime >= audioCue.time &&
                !playedAudioIndices.contains(index)
            ) {
                playSound(audioCue.cueType)
                playedAudioIndices.add(index)
            }
        }
    }

    private fun playSound(audioCueType: AudioCueType) {
        val soundId = soundMap[audioCueType]
        if (soundId != null) {
            soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
        } else {
            Log.e(TAG, "Sound not loaded for audio cue: $audioCueType")
        }
    }

    companion object {
        private const val TAG = "AudioManager"
    }
}

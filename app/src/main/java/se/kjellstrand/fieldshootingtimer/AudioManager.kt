package se.kjellstrand.fieldshootingtimer

import android.content.Context
import android.media.SoundPool
import android.util.Log

class AudioManager(context: Context) {
    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(5)
        .build()
    private val soundMap: MutableMap<Int, Int> = mutableMapOf()

    init {

        // Load sounds into the SoundPool
        soundMap[R.raw.tio_sekunder_kvar_cut] = soundPool.load(context, R.raw.tio_sekunder_kvar_cut, 1)
        soundMap[R.raw.fardiga_cut] = soundPool.load(context, R.raw.fardiga_cut, 1)
        soundMap[R.raw.eld_cut] = soundPool.load(context, R.raw.eld_cut, 1)
        soundMap[R.raw.eld_upp_hor_cut] = soundPool.load(context, R.raw.eld_upp_hor_cut, 1)
        soundMap[R.raw.patron_ur_proppa_vapen_cut] = soundPool.load(context, R.raw.patron_ur_proppa_vapen_cut, 1)
    }

    fun playSound(resId: Int) {
        val soundId = soundMap[resId]
        if (soundId != null) {
            soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
        } else {
            Log.e(Companion.TAG, "Sound not loaded for resource ID: $resId")
        }
    }

    fun release() {
        soundPool.release()
    }

    companion object {
        private const val TAG = "AudioManager"
    }
}

package se.kjellstrand.fieldshootingtimer.platform

import android.content.Context
import android.media.AudioManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

class AndroidPlatformAudioPolicy(context: Context) : PlatformAudioPolicy {
    private val systemAudioManager: AudioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    override fun shouldPlayCue(): Boolean =
        systemAudioManager.ringerMode == AudioManager.RINGER_MODE_NORMAL

    override fun shouldVibrate(): Boolean =
        systemAudioManager.ringerMode != AudioManager.RINGER_MODE_SILENT
}

@Composable
actual fun rememberPlatformAudioPolicy(): PlatformAudioPolicy {
    val context = LocalContext.current
    return remember(context) { AndroidPlatformAudioPolicy(context.applicationContext) }
}

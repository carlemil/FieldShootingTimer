package se.kjellstrand.fieldshootingtimer.platform

import android.app.NotificationManager
import android.content.Context
import android.media.AudioManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

class AndroidPlatformAudioPolicy(context: Context) : PlatformAudioPolicy {
    private val systemAudioManager: AudioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // While Do Not Disturb is active, getRingerMode() reports SILENT no
    // matter how the ringer switch is actually set — which used to mute both
    // cues and haptics for anyone running the timer with DND on. DND doesn't
    // mute media, and the cues are media the user explicitly started with
    // the play button, so DND itself never blocks; only a real silent/
    // vibrate ringer does (only readable while DND is off).
    private fun dndActive(): Boolean =
        notificationManager.currentInterruptionFilter !=
            NotificationManager.INTERRUPTION_FILTER_ALL

    override fun shouldPlayCue(): Boolean =
        dndActive() || systemAudioManager.ringerMode == AudioManager.RINGER_MODE_NORMAL

    override fun shouldVibrate(): Boolean =
        dndActive() || systemAudioManager.ringerMode != AudioManager.RINGER_MODE_SILENT
}

@Composable
actual fun rememberPlatformAudioPolicy(): PlatformAudioPolicy {
    val context = LocalContext.current
    return remember(context) { AndroidPlatformAudioPolicy(context.applicationContext) }
}

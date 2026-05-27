package se.kjellstrand.fieldshootingtimer.platform

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

private const val VIBRATION_LENGTH_MS = 300L

class AndroidHaptics(context: Context) : Haptics {
    private val vibrator: Vibrator? = context.getSystemService(Vibrator::class.java)

    override fun shortTick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(
                VibrationEffect.createOneShot(VIBRATION_LENGTH_MS, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        }
    }
}

@Composable
actual fun rememberHaptics(): Haptics {
    val context = LocalContext.current
    return remember(context) { AndroidHaptics(context.applicationContext) }
}

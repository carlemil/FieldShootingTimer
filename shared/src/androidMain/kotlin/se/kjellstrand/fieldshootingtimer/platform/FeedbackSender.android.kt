package se.kjellstrand.fieldshootingtimer.platform

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import java.util.Locale

private class AndroidFeedbackSender(private val context: Context) : FeedbackSender {

    override fun sendFeedback() {
        val (versionName, versionCode) = try {
            val info = context.packageManager.getPackageInfo(context.packageName, 0)
            info.versionName.orEmpty() to info.longVersionCode.toString()
        } catch (e: Exception) {
            "?" to "?"
        }
        val body = """


----
Debug:
App: $versionName ($versionCode)
OS: Android ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})
Enhet: ${Build.MANUFACTURER} ${Build.MODEL}
Språk: ${Locale.getDefault()}
"""
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = "mailto:".toUri()
            putExtra(Intent.EXTRA_EMAIL, arrayOf(FEEDBACK_EMAIL))
            putExtra(Intent.EXTRA_SUBJECT, "Ang. FältSkytteTimer appen på Android")
            putExtra(Intent.EXTRA_TEXT, body)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("FeedbackSender", "No mail client available", e)
        }
    }
}

@Composable
actual fun rememberFeedbackSender(): FeedbackSender {
    val context = LocalContext.current
    return remember(context) { AndroidFeedbackSender(context.applicationContext) }
}

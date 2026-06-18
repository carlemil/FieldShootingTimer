package se.kjellstrand.fieldshootingtimer.platform

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

class AndroidUrlOpener(private val context: Context) : UrlOpener {
    override fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}

@Composable
actual fun rememberUrlOpener(): UrlOpener {
    val context = LocalContext.current
    return remember(context) { AndroidUrlOpener(context.applicationContext) }
}

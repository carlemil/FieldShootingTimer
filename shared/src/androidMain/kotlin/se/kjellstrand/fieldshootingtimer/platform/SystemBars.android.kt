package se.kjellstrand.fieldshootingtimer.platform

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
actual fun SyncSystemBarsToTheme(darkTheme: Boolean) {
    val context = LocalContext.current
    val view = LocalView.current
    val window = remember(context) { context.findActivity()?.window } ?: return
    SideEffect {
        // Edge-to-edge already makes the bars transparent; without this the
        // 3-button navigation bar keeps an enforced near-opaque contrast
        // scrim that stays white in the app's dark mode.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        val controller = WindowCompat.getInsetsController(window, view)
        controller.isAppearanceLightStatusBars = !darkTheme
        controller.isAppearanceLightNavigationBars = !darkTheme
    }
}

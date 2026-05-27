package se.kjellstrand.fieldshootingtimer.platform

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

// iOS has no Material You equivalent; always return null so the static
// FieldShootingTimerTheme color scheme is used.
@Composable
actual fun dynamicColorScheme(dark: Boolean): ColorScheme? = null

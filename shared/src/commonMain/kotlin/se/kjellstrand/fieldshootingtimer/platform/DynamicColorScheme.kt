package se.kjellstrand.fieldshootingtimer.platform

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

/**
 * Returns a Material3 color scheme derived from the platform's dynamic
 * accent color, or null if the platform doesn't support dynamic theming.
 * Android (Material You, S+) returns dynamicLightColorScheme / dynamicDarkColorScheme;
 * iOS always returns null so the static fallback in FieldShootingTimerTheme is used.
 */
@Composable
expect fun dynamicColorScheme(dark: Boolean): ColorScheme?

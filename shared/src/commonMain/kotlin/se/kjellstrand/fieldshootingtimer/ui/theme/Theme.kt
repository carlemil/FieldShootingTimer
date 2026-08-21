package se.kjellstrand.fieldshootingtimer.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import se.kjellstrand.fieldshootingtimer.platform.dynamicColorScheme

// The semantic roles the app's composables draw from:
// background = the screen behind everything, surface/onSurface = cards and
// the command list, surfaceContainerHigh = the radial menu's floating
// buttons, onSurfaceVariant = secondary text, outline = borders,
// primary/onPrimary = the green action buttons, secondary/tertiary = the
// Fire (green) and CeaseFire (yellow) dial segments + play button,
// surfaceVariant = the gray dial segments, surfaceBright = the hand's and
// flag pennants' fill, secondaryContainer = the command-list highlight bar.
private val DarkColorScheme = darkColorScheme(
    primary = DarkModeGreenColor,
    // Black, not white: white on the dimmed green only measures 3.4:1 —
    // below WCAG's 4.5:1 for button text; black measures 5.4:1.
    onPrimary = BlackColor,
    secondary = DarkModeGreenColor,
    // The list highlight uses the same green as the dial's Fire segment.
    secondaryContainer = DarkModeGreenColor,
    onSecondaryContainer = BlackColor,
    tertiary = DarkModeYellowColor,
    background = DarkBackgroundColor,
    // Read by the dial's ink (contour, dividers, flags): muted gray, not
    // white — white outlines glared against the dark background.
    onBackground = DarkModeInkColor,
    surface = DarkSurfaceColor,
    onSurface = WhiteColor,
    // The gray dial segments.
    surfaceVariant = DarkModeGrayColor,
    onSurfaceVariant = DarkOnSurfaceVariantColor,
    // The radial menu's floating buttons.
    surfaceContainerHigh = DarkElevatedSurfaceColor,
    // The dial hand's and flag pennants' fill: mid gray, clearly darker
    // than the ink so nothing on the dial reads as near-white.
    surfaceBright = DarkModeFillGrayColor,
    // The hand's and flags' edge/pole color: one step lighter than the fill.
    outlineVariant = DarkModeEdgeGrayColor,
    outline = BlackColor
)

private val LightColorScheme = lightColorScheme(
    primary = LightGreenColor,
    onPrimary = BlackColor,
    secondary = LightGreenColor,
    // The list highlight uses the same green as the dial's Fire segment.
    secondaryContainer = LightGreenColor,
    onSecondaryContainer = BlackColor,
    tertiary = MutedYellowColor,
    background = GrayColor,
    onBackground = BlackColor,
    surface = WhiteColor,
    onSurface = BlackColor,
    // The gray dial segments.
    surfaceVariant = LightGrayColor,
    onSurfaceVariant = DimGrayColor,
    // The radial menu's floating buttons.
    surfaceContainerHigh = WhiteColor,
    // The dial hand's and flag pennants' fill.
    surfaceBright = WhiteColor,
    // The hand's and flags' edge/pole color.
    outlineVariant = BlackColor,
    outline = BlackColor
)

@Composable
fun FieldShootingTimerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is platform-dependent (Android 12+ returns a scheme; iOS returns null).
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val dynamic = if (dynamicColor) dynamicColorScheme(darkTheme) else null
    val colorScheme = dynamic ?: if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

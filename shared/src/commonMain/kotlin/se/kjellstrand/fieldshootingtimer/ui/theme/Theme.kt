package se.kjellstrand.fieldshootingtimer.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import se.kjellstrand.fieldshootingtimer.platform.dynamicColorScheme

private val DarkColorScheme = darkColorScheme(
    primary = LightGrayColor,
    secondary = LightGreenColor,
    tertiary = MutedYellowColor
)

private val LightColorScheme = lightColorScheme(
    primary = LightGrayColor,
    secondary = LightGreenColor,
    tertiary = MutedYellowColor
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

package se.kjellstrand.fieldshootingtimer.ui.theme

import androidx.compose.ui.graphics.Color

val WhiteColor = Color(0xFFF7F7F7)
val BlackColor = Color(0xFF0a0a0a)
val GrayColor = Color(0xFF777777)
// Secondary text on light surfaces: GrayColor only reaches 4.18:1 against
// WhiteColor, just under WCAG's 4.5:1 — this darker step measures 4.97:1.
val DimGrayColor = Color(0xFF6b6b6b)
val LightGrayColor = Color(0xFFaaaaaa)
val LightGreenColor = Color(0xFF4fdd44)
val PaleGreenColor = Color(0xFF9fdd44)
val TransparentGreenColor = Color(0x334fdd44)
val MutedYellowColor = Color(0xFFdddd44)
val RedColor = Color(0xFFdd4444)

// Dark-theme counterparts of the light theme's Gray/White surfaces.
val DarkBackgroundColor = Color(0xFF2e2e2e)
val DarkSurfaceColor = Color(0xFF1c1c1c)
val DarkOnSurfaceVariantColor = Color(0xFFb5b5b5)

// Dark-theme variants of the dial/accent colors — same hues, dimmed so
// they don't glare against the dark surfaces.
val DarkModeGreenColor = Color(0xFF2f9a28)
val DarkModePaleGreenColor = Color(0xFF8fb63c)
val DarkModeYellowColor = Color(0xFFa8a832)
val DarkModeGrayColor = Color(0xFF6e6e6e)

// Dark-theme floating buttons (radial menu): lighter than the background so
// they read as raised — the card surface color drowns against it.
val DarkElevatedSurfaceColor = Color(0xFF474747)

// Dark-theme dial ink (contour, dividers, badge borders): a muted gray —
// pure white glares against the dark background; this still measures 6.9:1
// against it.
val DarkModeInkColor = Color(0xFFb8b8b8)

// Dark-theme hand and flags: two clearly darker gray steps — a mid-gray
// fill with a lighter edge — so they read as gray instruments rather than
// near-white, while the edge keeps them visible against both the dark
// background (4.7:1) and the gray segments.
val DarkModeEdgeGrayColor = Color(0xFF9e9e9e)
val DarkModeFillGrayColor = Color(0xFF858585)

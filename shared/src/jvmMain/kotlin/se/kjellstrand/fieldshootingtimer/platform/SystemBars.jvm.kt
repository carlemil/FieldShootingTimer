package se.kjellstrand.fieldshootingtimer.platform

import androidx.compose.runtime.Composable

// No system bars on the desktop test host.
@Composable
actual fun SyncSystemBarsToTheme(darkTheme: Boolean) = Unit

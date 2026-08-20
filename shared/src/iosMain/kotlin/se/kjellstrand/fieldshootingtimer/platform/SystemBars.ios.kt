package se.kjellstrand.fieldshootingtimer.platform

import androidx.compose.runtime.Composable

// iOS has no navigation bar to restyle; the status bar follows the
// view-controller appearance and the home indicator is self-coloring.
@Composable
actual fun SyncSystemBarsToTheme(darkTheme: Boolean) = Unit

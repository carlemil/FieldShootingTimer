package se.kjellstrand.fieldshootingtimer.platform

import androidx.compose.runtime.Composable

/**
 * Keeps the system bars (status bar and the back/home/recents navigation
 * bar) in step with the app's own light/dark theme — the theme is an in-app
 * choice, so the bars can't just follow the system setting. Android drops
 * the navigation bar's enforced contrast scrim (the white strip behind the
 * 3-button bar) and flips the icon appearance; iOS and jvm are no-ops (no
 * such bars to manage).
 */
@Composable
expect fun SyncSystemBarsToTheme(darkTheme: Boolean)

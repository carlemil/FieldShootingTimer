package se.kjellstrand.fieldshootingtimer.platform

import androidx.compose.runtime.Composable

/**
 * While [enabled] is true, prevents the device from auto-sleeping the
 * screen. Restores normal behavior when [enabled] becomes false or when
 * the composable leaves composition. Android wires FLAG_KEEP_SCREEN_ON on
 * the host activity's Window; iOS sets `UIApplication.idleTimerDisabled`.
 */
@Composable
expect fun KeepScreenOn(enabled: Boolean)

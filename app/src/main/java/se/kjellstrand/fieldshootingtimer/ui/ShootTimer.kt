package se.kjellstrand.fieldshootingtimer.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import se.kjellstrand.fieldshootingtimer.ui.theme.CeaseFireSegmentColor
import se.kjellstrand.fieldshootingtimer.ui.theme.HandBackgroundColor
import se.kjellstrand.fieldshootingtimer.ui.theme.PlugWeaponSegmentColor
import se.kjellstrand.fieldshootingtimer.ui.theme.PrepareSegmentColor
import se.kjellstrand.fieldshootingtimer.ui.theme.ShootSegmentColor
import se.kjellstrand.fieldshootingtimer.ui.theme.TimerBordersColor

@Composable
fun ShootTimer(
    timerUiState: TimerUiState,
    segmentDurations: List<Float>,
    ticks: List<Int>,
    timerSize: Dp
) {
    Box(
        contentAlignment = Alignment.Center
    ) {
        Timer(
            currentTime = timerUiState.currentTime,
            dialColors = DialColors(
                colors = listOf(
                    PrepareSegmentColor,
                    PrepareSegmentColor,
                    ShootSegmentColor,
                    CeaseFireSegmentColor,
                    PlugWeaponSegmentColor,
                    PrepareSegmentColor,
                    PrepareSegmentColor
                    )
            ),
            gapAngleDegrees = 30f,
            timesForSegments = segmentDurations,
            ticks = ticks,
            ringThickness = 60.dp,
            borderColor = TimerBordersColor,
            borderWidth = 2.dp,
            size = timerSize,
            badgeRadius = 15.dp,
            handColor = HandBackgroundColor,
            handThickness = 8.dp,
            badgesVisible = timerUiState.badgesVisible
        )
    }
}
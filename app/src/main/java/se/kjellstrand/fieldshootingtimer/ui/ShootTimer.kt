package se.kjellstrand.fieldshootingtimer.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import se.kjellstrand.fieldshootingtimer.ui.theme.MutedYellowColor
import se.kjellstrand.fieldshootingtimer.ui.theme.WhiteColor
import se.kjellstrand.fieldshootingtimer.ui.theme.RedColor
import se.kjellstrand.fieldshootingtimer.ui.theme.LightGrayColor
import se.kjellstrand.fieldshootingtimer.ui.theme.LightGreenColor
import se.kjellstrand.fieldshootingtimer.ui.theme.BlackColor

@Composable
fun ShootTimer(
    timerUiState: TimerUiState,
    segmentDurations: List<Float>,
    timerSize: Dp
) {
    Box(
        contentAlignment = Alignment.Center
    ) {
        Timer(
            currentTime = timerUiState.currentTime,
            segmentColors = listOf(
                LightGrayColor,
                LightGrayColor,
                LightGreenColor,
                MutedYellowColor,
                RedColor,
                LightGrayColor,
                LightGrayColor
            ),
            gapAngleDegrees = 30f,
            timesForSegments = segmentDurations,
            ticks = timerUiState.thumbValues.map { it },
            ringThickness = 60.dp,
            borderColor = BlackColor,
            borderWidth = 2.dp,
            size = timerSize,
            badgeRadius = 15.dp,
            handColor = WhiteColor,
            handThickness = 8.dp,
            badgesVisible = timerUiState.badgesVisible
        )
    }
}
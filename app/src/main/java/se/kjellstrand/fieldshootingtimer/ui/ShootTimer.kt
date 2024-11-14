package se.kjellstrand.fieldshootingtimer.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import se.kjellstrand.fieldshootingtimer.ui.theme.MutedYellowColor
import se.kjellstrand.fieldshootingtimer.ui.theme.WhiteColor
import se.kjellstrand.fieldshootingtimer.ui.theme.RedColor
import se.kjellstrand.fieldshootingtimer.ui.theme.LightGrayColor
import se.kjellstrand.fieldshootingtimer.ui.theme.LightGreenColor
import se.kjellstrand.fieldshootingtimer.ui.theme.BlackColor

@Composable
fun ShootTimer(
    timerViewModel: TimerViewModel,
    segmentDurations: List<Float>,
    timerSize: Dp
) {
    val currentTime by timerViewModel.currentTimeFlow.collectAsState(
        initial = 0f, context = Dispatchers.Main
    )
    val badgesVisible by timerViewModel.badgesVisibleFlow.collectAsState(
        initial = false, context = Dispatchers.Main
    )
    val thumbValues by timerViewModel.thumbValuesFlow.collectAsState(
        initial = listOf(), context = Dispatchers.Main
    )
    Box(
        contentAlignment = Alignment.Center
    ) {
        Timer(
            currentTime = currentTime,
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
            ticks = thumbValues,
            ringThickness = 60.dp,
            borderColor = BlackColor,
            borderWidth = 2.dp,
            size = timerSize,
            badgeRadius = 15.dp,
            handColor = WhiteColor,
            handThickness = 8.dp,
            badgesVisible = badgesVisible
        )
    }
}
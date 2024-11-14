package se.kjellstrand.fieldshootingtimer.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import se.kjellstrand.fieldshootingtimer.ui.theme.BlackColor
import se.kjellstrand.fieldshootingtimer.ui.theme.FieldShootingTimerTheme
import se.kjellstrand.fieldshootingtimer.ui.theme.WhiteColor

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
        val segmentColors = Command.entries.filter { it.duration >= 0 }.map { it.color }
        val gapAngleDegrees = 30f
        val borderWidth = 2.dp

        val totalSeconds = segmentDurations.sum()

        require(totalSeconds > 0) {
            "Total time must be greater than 0."
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(timerSize)
        ) {
            DecoratedDial(
                segmentColors = segmentColors,
                gapAngleDegrees = gapAngleDegrees,
                segments = segmentDurations,
                ticks = thumbValues,
                ringThickness = 60.dp,
                borderColor = BlackColor,
                borderWidth = borderWidth,
                size = timerSize,
                segmentBadgesVisible = badgesVisible,
                badgeRadius = 15.dp
            )

            DialHand(
                currentTime = currentTime,
                totalTime = totalSeconds,
                gapAngleDegrees = gapAngleDegrees,
                size = timerSize,
                borderWidth = borderWidth,
                handColor = WhiteColor,
                borderColor = BlackColor,
                handThickness = 8.dp,
                overshootPercent = 0.1f // 10% overshoot
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DialWithHandPreview() {
    FieldShootingTimerTheme {
        val tvm = TimerViewModel()
        val timeInSecondsForEachSegment = listOf(7f, 3f, 2f, 3f, 1f)

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            ShootTimer(
                tvm, timeInSecondsForEachSegment, 200.dp
            )
        }
    }
}
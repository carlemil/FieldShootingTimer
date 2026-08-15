package se.kjellstrand.fieldshootingtimer.ui

import se.kjellstrand.fieldshootingtimer.domain.Command

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import se.kjellstrand.fieldshootingtimer.domain.fireStartSeconds
import se.kjellstrand.fieldshootingtimer.ui.theme.BlackColor
import se.kjellstrand.fieldshootingtimer.ui.theme.Paddings
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
    val thumbValues by timerViewModel.thumbValuesFlow.collectAsState(
        initial = listOf(), context = Dispatchers.Main
    )
    val timerRunningState by timerViewModel.timerRunningStateFlow.collectAsState(
        initial = TimerRunningState.NotStarted, context = Dispatchers.Main
    )
    val range by timerViewModel.rangeFlow.collectAsState(
        context = Dispatchers.Main
    )
    Box(
        contentAlignment = Alignment.Center
    ) {
        val segmentColors = Command.timedCommands.map { it.color }
        val gapAngleDegrees = 30f
        val borderWidth = 2.dp
        val ringThickness = 60.dp

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
                ringThickness = ringThickness,
                borderColor = BlackColor,
                borderWidth = borderWidth,
                size = timerSize,
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
                handThickness = Paddings.Small,
                overshootPercent = 0.1f // 10% overshoot
            )

            DialGestureOverlay(
                size = timerSize,
                ticks = thumbValues,
                ticksMax = totalSeconds.toInt(),
                range = range,
                fireStart = fireStartSeconds(),
                fireDuration = segmentDurations.getOrNull(Command.fireSegmentIndex) ?: 0f,
                gapAngleDegrees = gapAngleDegrees,
                ringThickness = ringThickness,
                enabled = timerRunningState == TimerRunningState.NotStarted,
                onDragSetTicks = timerViewModel::setThumbValues,
                onDragRoundTicks = timerViewModel::roundThumbValues,
                onPinchSetShootingDuration = timerViewModel::setShootingTime
            )
        }
    }
}

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
        val segmentColors = Command.dialCommands.map { it.color }
        val gapAngleDegrees = 30f
        val borderWidth = 2.dp
        val ringThickness = 60.dp

        // The dial only draws the segments through CeaseFire; the timer keeps
        // running past them (UnloadWeapon + Visitation audio cues and list
        // highlight are unaffected) while the hand parks at the dial's end.
        // dialCommands is a prefix of timedCommands, so a plain take() slices
        // the matching durations.
        val dialSegments = segmentDurations.take(Command.dialCommands.size)
        val dialSeconds = dialSegments.sum()

        require(dialSeconds > 0) {
            "Total time must be greater than 0."
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(timerSize)
        ) {
            DecoratedDial(
                segmentColors = segmentColors,
                gapAngleDegrees = gapAngleDegrees,
                segments = dialSegments,
                ticks = thumbValues,
                ringThickness = ringThickness,
                borderColor = BlackColor,
                borderWidth = borderWidth,
                size = timerSize,
                badgeRadius = 15.dp
            )

            DialHand(
                // Negative during a competition countdown (hand waits at 0);
                // past the dial's end during UnloadWeapon/Visitation (parks).
                currentTime = currentTime.coerceIn(0f, dialSeconds),
                totalTime = dialSeconds,
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
                ticksMax = dialSeconds.toInt(),
                range = range,
                fireStart = fireStartSeconds(),
                fireDuration = segmentDurations.getOrNull(Command.fireSegmentIndex) ?: 0f,
                currentTime = currentTime,
                gapAngleDegrees = gapAngleDegrees,
                ringThickness = ringThickness,
                editEnabled = timerRunningState == TimerRunningState.NotStarted,
                // The hand is draggable whenever the timer isn't running.
                scrubEnabled = timerRunningState != TimerRunningState.Running,
                onDragSetTicks = timerViewModel::setThumbValues,
                onDragRoundTicks = timerViewModel::roundThumbValues,
                onPinchSetShootingDuration = timerViewModel::setShootingTime,
                onScrub = timerViewModel::scrubTo
            )
        }
    }
}

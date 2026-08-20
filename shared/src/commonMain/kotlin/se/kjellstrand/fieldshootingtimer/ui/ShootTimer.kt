package se.kjellstrand.fieldshootingtimer.ui

import se.kjellstrand.fieldshootingtimer.domain.Command

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import se.kjellstrand.fieldshootingtimer.domain.fireStartSeconds
import se.kjellstrand.fieldshootingtimer.ui.theme.Paddings

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
        // All segments follow the theme so dark mode gets dimmed variants:
        // green = secondary, yellow = tertiary, gray = surfaceVariant.
        val segmentColors = Command.dialCommands.map { command ->
            when (command) {
                Command.Fire -> MaterialTheme.colorScheme.secondary
                Command.CeaseFire -> MaterialTheme.colorScheme.tertiary
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        }
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
            // The dial's "ink" (contour, dividers, ticks, badge borders, hand
            // border) follows onBackground: black in light, white in dark —
            // hardcoded black measured 1.46:1 against the dark background.
            val dialInk = MaterialTheme.colorScheme.onBackground
            DecoratedDial(
                segmentColors = segmentColors,
                gapAngleDegrees = gapAngleDegrees,
                segments = dialSegments,
                ticks = thumbValues,
                ringThickness = ringThickness,
                borderColor = dialInk,
                borderWidth = borderWidth,
                size = timerSize,
                badgeRadius = 15.dp
            )

            // While running, sample the run's elapsed time once per display
            // frame instead of drawing the timer loop's tick emissions — the
            // 16ms delay-loop cadence drifts in and out of phase with vsync,
            // which made the hand visibly judder. Paused/parked states follow
            // the collected currentTime (seek, scrub, reset).
            // remember(running), not produceState: the state must re-seed
            // from currentTime the instant running flips, or a run started
            // after a seek flashes the previous run's last hand position for
            // one frame (produceState keeps its value across key restarts).
            val running = timerRunningState == TimerRunningState.Running
            val handTime = remember(running) { mutableStateOf(currentTime) }
            if (running) {
                LaunchedEffect(Unit) {
                    while (true) {
                        withFrameMillis { }
                        timerViewModel.frameTimeSeconds()?.let { handTime.value = it }
                    }
                }
            }

            DialHand(
                // Negative during a competition countdown (hand waits at 0);
                // past the dial's end during UnloadWeapon/Visitation (parks).
                currentTime = (if (running) handTime.value else currentTime)
                    .coerceIn(0f, dialSeconds),
                totalTime = dialSeconds,
                gapAngleDegrees = gapAngleDegrees,
                size = timerSize,
                borderWidth = borderWidth,
                // surfaceBright/outlineVariant: white with black edge in
                // light; two darker gray steps in dark.
                handColor = MaterialTheme.colorScheme.surfaceBright,
                borderColor = MaterialTheme.colorScheme.outlineVariant,
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

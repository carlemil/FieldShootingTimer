package se.kjellstrand.fieldshootingtimer.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.Dispatchers
import se.kjellstrand.fieldshootingtimer.domain.COMPETITION_ALL_READY_REMAINING_SECONDS
import kotlin.math.ceil

/**
 * Remaining whole seconds of the CURRENT countdown phase while
 * [currentTime] is negative, else null: the 60s Ladda stretch counts 60→1,
 * the Alla klara wait 10→1 — and the repeated wait after "Fråga igen"
 * ([allReadyRepeat]) counts its full span.
 */
internal fun countdownSecondsOrNull(
    currentTime: Float,
    allReadyRepeat: Boolean = false
): Int? {
    if (currentTime >= 0f) return null
    val remaining = ceil(-currentTime).toInt()
    return if (!allReadyRepeat && currentTime < -COMPETITION_ALL_READY_REMAINING_SECONDS) {
        remaining - COMPETITION_ALL_READY_REMAINING_SECONDS.toInt()
    } else {
        remaining
    }
}

/**
 * The dial with the play/stop/reset button overlaid at its center. During a
 * competition-mode preparation countdown (negative currentTime) the play
 * button shows the remaining seconds.
 */
@Composable
internal fun TimerWithPlayButton(
    timerViewModel: TimerViewModel,
    segmentDurations: List<Float>,
    onClickPlayButton: () -> Unit,
    timerRunningState: TimerRunningState,
    timerSize: Dp,
    modifier: Modifier = Modifier
) {
    val currentTime by timerViewModel.currentTimeFlow.collectAsState(
        initial = 0f, context = Dispatchers.Main
    )
    val allReadyRepeat by timerViewModel.allReadyRepeatFlow.collectAsState(
        initial = false, context = Dispatchers.Main
    )
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        ShootTimer(
            timerViewModel = timerViewModel,
            segmentDurations = segmentDurations,
            timerSize = timerSize
        )
        PlayButton(
            onClickPlayButton = onClickPlayButton,
            timerRunningState = timerRunningState,
            timerSize = timerSize,
            // Digits only while actually counting down — a timer parked at a
            // negative time by seekTo shows the plain play icon instead.
            countdownSeconds = countdownSecondsOrNull(currentTime, allReadyRepeat)
                ?.takeIf { timerRunningState == TimerRunningState.Running }
        )
    }
}

package se.kjellstrand.fieldshootingtimer.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.Dispatchers
import se.kjellstrand.fieldshootingtimer.ui.theme.Paddings
import kotlin.math.ceil

/** Remaining whole countdown seconds while [currentTime] is negative, else null. */
internal fun countdownSecondsOrNull(currentTime: Float): Int? =
    if (currentTime < 0f) ceil(-currentTime).toInt() else null

/**
 * The dial with the play/stop/reset button overlaid at its center and the
 * tick +/- buttons hanging below the lower-right corner. During a
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
    val range by timerViewModel.rangeFlow.collectAsState(
        context = Dispatchers.Main
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
            countdownSeconds = countdownSecondsOrNull(currentTime)
        )
        TicksAdjuster(
            enabled = timerRunningState == TimerRunningState.NotStarted,
            setThumbValuesMinusOne = timerViewModel::dropLastThumbValue,
            setThumbValuesPlusOne = { timerViewModel.addNewThumbValue(range) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(horizontal = Paddings.Medium)
                // Hang slightly below the dial's lower edge, out of its way.
                .offset(y = Paddings.Large)
        )
    }
}

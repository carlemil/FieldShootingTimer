package se.kjellstrand.fieldshootingtimer.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.Dispatchers
import se.kjellstrand.fieldshootingtimer.ui.theme.BlackColor
import se.kjellstrand.fieldshootingtimer.ui.theme.Paddings
import kotlin.math.ceil

internal const val COUNTDOWN_TEXT_TAG = "CountdownText"

/**
 * The dial with the play/stop/reset button overlaid at its center, the tick
 * +/- buttons in the lower-left corner, and — during a competition-mode
 * preparation countdown (negative currentTime) — the remaining seconds shown
 * below the play button.
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
            timerSize = timerSize
        )
        if (currentTime < 0f) {
            Text(
                text = ceil(-currentTime).toInt().toString(),
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                color = BlackColor,
                modifier = Modifier
                    .align(Alignment.Center)
                    // Between the play button's lower edge (timerSize/6 from
                    // center) and the ring's inner edge.
                    .offset(y = timerSize / 4)
                    .testTag(COUNTDOWN_TEXT_TAG)
            )
        }
        TicksAdjuster(
            enabled = timerRunningState == TimerRunningState.NotStarted,
            setThumbValuesMinusOne = timerViewModel::dropLastThumbValue,
            setThumbValuesPlusOne = { timerViewModel.addNewThumbValue(range) },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(Paddings.Medium)
        )
    }
}

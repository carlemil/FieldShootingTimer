package se.kjellstrand.fieldshootingtimer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import se.kjellstrand.fieldshootingtimer.ui.theme.GrayColor
import se.kjellstrand.fieldshootingtimer.ui.theme.Paddings

@Composable
fun PortraitLayout(
    timerViewModel: TimerViewModel,
    segmentDurations: List<Float>,
    onClickPlayButton: () -> Unit,
    timerRunningState: TimerRunningState,
    statelessSettingsComposable: @Composable () -> Unit,
    timerSize: Dp
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(GrayColor)
            .systemBarsPadding()
    ) {
        Spacer(modifier = Modifier.padding(Paddings.Small))
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Paddings.Large)
        ) {
            ShootTimer(timerViewModel, segmentDurations, timerSize)
            PlayButton(
                onClickPlayButton = onClickPlayButton,
                timerRunningState = timerRunningState,
                timerSize = timerSize
            )
        }
        Spacer(modifier = Modifier.padding(Paddings.Medium))
        statelessSettingsComposable()
    }
}

@Preview(showBackground = true)
@Composable
fun PortraitLayoutPreview() {
    val tvm = TimerViewModel()
    tvm.setShootingTime(5f)
    tvm.setCurrentTime(0f)
    tvm.setTimerState(TimerRunningState.NotStarted)

    val segmentDurations = listOf(7f, 3f, 5f, 3f, 4f, 2f)
    val settings: @Composable () -> Unit = {
        SettingsPanel(tvm, IntRange(5, 12), segmentDurations)
    }
    PortraitLayout(
        tvm,
        segmentDurations,
        {},
        TimerRunningState.NotStarted,
        settings,
        300.dp
    )
}

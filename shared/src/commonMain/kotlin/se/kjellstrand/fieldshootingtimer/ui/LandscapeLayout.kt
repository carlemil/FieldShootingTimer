package se.kjellstrand.fieldshootingtimer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import se.kjellstrand.fieldshootingtimer.ui.theme.GrayColor
import se.kjellstrand.fieldshootingtimer.ui.theme.Paddings

@Composable
fun LandscapeLayout(
    timerViewModel: TimerViewModel,
    segmentDurations: List<Float>,
    onClickPlayButton: () -> Unit,
    timerRunningState: TimerRunningState,
    statelessSettingsComposable: @Composable () -> Unit,
    timerSize: Dp
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(GrayColor)
            .systemBarsPadding()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxHeight()
                .padding(start = 8.dp)
                .navigationBarsPadding()
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                ShootTimer(timerViewModel, segmentDurations, timerSize)
                PlayButton(
                    onClickPlayButton = onClickPlayButton,
                    timerRunningState = timerRunningState,
                    timerSize = timerSize
                )
            }

        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxHeight()
                .padding(Paddings.Tiny)
                .navigationBarsPadding()
        ) {
            statelessSettingsComposable()
        }
    }
}

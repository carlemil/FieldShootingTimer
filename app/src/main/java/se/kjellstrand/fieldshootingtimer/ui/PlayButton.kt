package se.kjellstrand.fieldshootingtimer.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.Dp

@Composable
fun PlayButton(
    timerViewModel: TimerViewModel,
    timerSize: Dp
) {
    val timerUiState by timerViewModel.uiState.collectAsState()
    TimerStateButton(
        timerUiState = timerUiState,
        buttonSize = timerSize / 2f,
        onPlayStopResetClicked = {
            when (timerUiState.timerRunningState) {
                TimerState.NotStarted -> {
                    timerViewModel.setTimerState(TimerState.Running)
                }

                TimerState.Running -> {
                    timerViewModel.setTimerState(TimerState.Stopped)
                }

                TimerState.Stopped, TimerState.Finished -> {
                    timerViewModel.setCurrentTime(0f)
                    timerViewModel.setTimerState(TimerState.NotStarted)
                }
            }
        }
    )
}
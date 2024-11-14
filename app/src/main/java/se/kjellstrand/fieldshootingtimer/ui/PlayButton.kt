package se.kjellstrand.fieldshootingtimer.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.Dispatchers

@Composable
fun PlayButton(
    timerViewModel: TimerViewModel,
    timerSize: Dp
) {
    val timerRunningState by timerViewModel.timerRunningStateFlow.collectAsState(
        initial = TimerState.NotStarted, context = Dispatchers.Main
    )
    Box(
        contentAlignment = Alignment.Center
    ) {
        TimerStateButton(
            timerViewModel = timerViewModel,
            buttonSize = timerSize / 3f,
            onPlayStopResetClicked = {
                when (timerRunningState) {
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
}
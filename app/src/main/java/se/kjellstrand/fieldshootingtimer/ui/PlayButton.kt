package se.kjellstrand.fieldshootingtimer.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import se.kjellstrand.fieldshootingtimer.R
import se.kjellstrand.fieldshootingtimer.ui.theme.BlackColor
import se.kjellstrand.fieldshootingtimer.ui.theme.LightGreenColor

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
        val buttonSize = timerSize / 3f
        OutlinedButton(
            onClick = {
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
            },
            modifier = Modifier.size(buttonSize),
            shape = CircleShape,
            contentPadding = PaddingValues(0.dp),
            border = BorderStroke(2.dp, BlackColor),
            colors = ButtonDefaults.buttonColors(
                containerColor = LightGreenColor
            )
        ) {
            when (timerRunningState) {
                TimerState.NotStarted -> {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.play_arrow),
                        contentDescription = "Play",
                        modifier = Modifier.size(buttonSize * 0.8f)
                    )
                }

                TimerState.Running -> {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.stop),
                        contentDescription = "Stop",
                        modifier = Modifier.size(buttonSize * 0.8f)
                    )
                }

                TimerState.Finished, TimerState.Stopped -> {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.skip_previous),
                        contentDescription = "Reset",
                        modifier = Modifier.size(buttonSize * 0.8f)
                    )
                }
            }
        }
    }
}

package se.kjellstrand.fieldshootingtimer.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import se.kjellstrand.fieldshootingtimer.ui.theme.BlackColor
import se.kjellstrand.fieldshootingtimer.ui.theme.LightGreenColor
import se.kjellstrand.fieldshootingtimer.ui.theme.Paddings

@Composable
fun PlayButton(
    onClickPlayButton: () -> Unit,
    timerRunningState: TimerRunningState,
    timerSize: Dp
) {
    Box(
        contentAlignment = Alignment.Center
    ) {
        val buttonSize = timerSize / 3f
        OutlinedButton(
            onClick = onClickPlayButton,
            modifier = Modifier.size(buttonSize),
            shape = CircleShape,
            contentPadding = PaddingValues(0.dp),
            border = BorderStroke(Paddings.Tiny, BlackColor),
            colors = ButtonDefaults.buttonColors(
                containerColor = LightGreenColor
            )
        ) {
            when (timerRunningState) {
                // TODO: project vector drawables (R.drawable.play_arrow / stop /
                // skip_previous) wired back in shared/compose-resources. Using
                // Material icons as a placeholder for the build to compile.
                TimerRunningState.NotStarted -> {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "Play",
                        modifier = Modifier.size(buttonSize * 0.8f)
                    )
                }

                TimerRunningState.Running -> {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Stop",
                        modifier = Modifier.size(buttonSize * 0.8f)
                    )
                }

                TimerRunningState.Finished, TimerRunningState.Stopped -> {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Reset",
                        modifier = Modifier.size(buttonSize * 0.8f)
                    )
                }
            }
        }
    }
}

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import se.kjellstrand.fieldshootingtimer.R
import se.kjellstrand.fieldshootingtimer.ui.theme.BlackColor
import se.kjellstrand.fieldshootingtimer.ui.theme.LightGreenColor
import se.kjellstrand.fieldshootingtimer.ui.theme.Paddings

@Composable
fun PlayButton(
    onClickPlayButton: () -> Unit,
    timerRunningState: TimerState,
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

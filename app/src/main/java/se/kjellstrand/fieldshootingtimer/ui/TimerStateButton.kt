package se.kjellstrand.fieldshootingtimer.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import se.kjellstrand.fieldshootingtimer.R
import se.kjellstrand.fieldshootingtimer.ui.theme.ShootSegmentColor
import se.kjellstrand.fieldshootingtimer.ui.theme.TimerBordersColor

@Composable
fun TimerStateButton(
    timerUiState: TimerUiState,
    onPlayStopResetClicked: () -> Unit,
    buttonSize: Dp = 56.dp
) {
    OutlinedButton(
        onClick = { onPlayStopResetClicked() },
        modifier = Modifier.size(buttonSize),
        shape = CircleShape,
        contentPadding = PaddingValues(0.dp),
        border = BorderStroke(2.dp, TimerBordersColor),
        colors = ButtonDefaults.buttonColors(
            containerColor = ShootSegmentColor
        )
    ) {
        when(timerUiState.timerRunningState) {
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
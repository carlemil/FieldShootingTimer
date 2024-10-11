package se.kjellstrand.fieldshootingtimer

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ControlButton(
    isRunning: Boolean,
    isFinished: Boolean,
    onPlayPauseResetClicked: () -> Unit,
    buttonSize: Dp = 56.dp
) {
    Button(
        onClick = { onPlayPauseResetClicked() },
        modifier = Modifier.size(buttonSize),
        shape = CircleShape,
        contentPadding = PaddingValues(0.dp),
    ) {
        when {
            isFinished -> {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.skip_previous),
                    contentDescription = "Reset",
                    modifier = Modifier.size(buttonSize * 0.6f)
                )
            }

            isRunning -> {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.stop),
                    contentDescription = "Stop",
                    modifier = Modifier.size(buttonSize * 0.6f)
                )
            }

            else -> {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.play_arrow),
                    contentDescription = "Play",
                    modifier = Modifier.size(buttonSize * 0.6f)
                )
            }
        }
    }
}
package se.kjellstrand.fieldshootingtimer.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import se.kjellstrand.fieldshootingtimer.R
import se.kjellstrand.fieldshootingtimer.ui.theme.LightGreenColor
import se.kjellstrand.fieldshootingtimer.ui.theme.Paddings
import se.kjellstrand.fieldshootingtimer.ui.theme.TransparentGreenColor
import se.kjellstrand.fieldshootingtimer.ui.theme.PaleGreenColor

@Composable
fun ShootTimeSlider(
    shootingDuration: Float,
    timerRunningState: TimerState,
    onValueChange: (Float) -> Unit
) {
    val onValueChangeState by rememberUpdatedState(onValueChange)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Slider(
            value = shootingDuration,
            enabled = timerRunningState == TimerState.NotStarted,
            onValueChange = { value -> onValueChangeState(value) },
            onValueChangeFinished = { },
            colors = SliderDefaults.colors(
                thumbColor = PaleGreenColor,
                activeTrackColor = LightGreenColor,
                inactiveTrackColor = TransparentGreenColor
            ),
            valueRange = 1f..27f,
            modifier = Modifier
                .padding(start = Paddings.Large, end = Paddings.Large)
                .weight(1f)
        )
        Text(
            text = stringResource(
                R.string.shooting_time,
                (shootingDuration + Command.CeaseFire.duration).toInt()
            ),
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            modifier = Modifier
                .padding(end = Paddings.Large)
        )
    }
}
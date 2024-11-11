package se.kjellstrand.fieldshootingtimer.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import se.kjellstrand.fieldshootingtimer.CEASE_FIRE_DURATION
import se.kjellstrand.fieldshootingtimer.R
import se.kjellstrand.fieldshootingtimer.ui.theme.SliderActiveTrackColor
import se.kjellstrand.fieldshootingtimer.ui.theme.SliderInactiveTrackColor
import se.kjellstrand.fieldshootingtimer.ui.theme.SliderThumbColor
import kotlin.math.roundToInt

@Composable
fun ShootTimeSlider(
    timerViewModel: TimerViewModel,
    playedAudioIndices: MutableSet<Int>
) {
    val timerUiState by timerViewModel.uiState.collectAsState()
    val shootingDuration = timerUiState.shootingDuration
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = stringResource(
                R.string.shooting_time,
                (shootingDuration + CEASE_FIRE_DURATION).toInt()
            ),
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp
        )
        Slider(
            value = shootingDuration,
            enabled = timerUiState.timerRunningState == TimerState.NotStarted,
            onValueChange = { newShootingDuration ->
                timerViewModel.setShootingTime(newShootingDuration.roundToInt().toFloat())
                playedAudioIndices.clear()
            },
            onValueChangeFinished = { },
            colors = SliderDefaults.colors(
                thumbColor = SliderThumbColor,
                activeTrackColor = SliderActiveTrackColor,
                inactiveTrackColor = SliderInactiveTrackColor
            ),
            valueRange = 1f..27f,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}
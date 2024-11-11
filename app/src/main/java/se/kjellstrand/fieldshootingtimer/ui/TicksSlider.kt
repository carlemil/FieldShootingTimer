package se.kjellstrand.fieldshootingtimer.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import se.kjellstrand.fieldshootingtimer.findNextFreeThumbSpot
import se.kjellstrand.fieldshootingtimer.ui.theme.SliderInactiveTrackColor
import se.kjellstrand.fieldshootingtimer.ui.theme.SliderThumbColor

@Composable
fun TicksSlider(
    timerViewModel: TimerViewModel,
    range: IntRange,
) {
    val timerUiState by timerViewModel.uiState.collectAsState()

    Row(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "+",
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(end = 8.dp)
                .clickable {
                    if (timerUiState.thumbValues.size < (range.last - range.first)) {
                        timerViewModel.setThumbValues(
                            timerUiState.thumbValues +
                                    findNextFreeThumbSpot(range, timerUiState.thumbValues)
                        )
                    }
                }
        )

        MultiThumbSlider(
            timerViewModel, range,
            trackColor = SliderInactiveTrackColor,
            thumbColor = SliderThumbColor,
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically)
                .padding(horizontal = 8.dp)
        )

        Text(
            text = "-",
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(start = 8.dp)
                .clickable {
                    timerViewModel.setThumbValues(timerUiState.thumbValues.dropLast(1))
                }
        )
    }
}
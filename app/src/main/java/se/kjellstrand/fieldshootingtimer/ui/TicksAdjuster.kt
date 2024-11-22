package se.kjellstrand.fieldshootingtimer.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import se.kjellstrand.fieldshootingtimer.ui.theme.Paddings
import se.kjellstrand.fieldshootingtimer.ui.theme.PaleGreenColor
import se.kjellstrand.fieldshootingtimer.ui.theme.TransparentGreenColor
import kotlin.math.roundToInt

@Composable
fun TicksAdjuster(
    timerViewModel: TimerViewModel,
    range: IntRange,
) {
    val setThumbValuesMinusOne = rememberUpdatedState { thumbValues: List<Float> ->
        timerViewModel.setThumbValues(thumbValues.dropLast(1))
    }
    val thumbValues by timerViewModel.thumbValuesFlow.collectAsState(
        initial = listOf(), context = Dispatchers.Main
    )

    val onHorizontalDragSetThumbValues = rememberUpdatedState{ newThumbValues: List<Float> ->
        timerViewModel.setThumbValues(newThumbValues)
    }
    val onHorizontalDragRoundThumbValues = rememberUpdatedState {
        timerViewModel.roundThumbValues()
    }

    Row(modifier = Modifier.padding(horizontal = Paddings.Large)) {
        Text(
            text = "+",
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(end = Paddings.Small)
                .clickable {
                    if (thumbValues.size < (range.last - range.first)) {
                        timerViewModel.setThumbValues(
                            thumbValues + findNextFreeThumbSpot(range, thumbValues)
                        )
                    }
                }
        )

        MultiThumbSlider(
            thumbValues = thumbValues,
            onHorizontalDragSetThumbValues = onHorizontalDragSetThumbValues,
            onHorizontalDragRoundThumbValues = onHorizontalDragRoundThumbValues,
            range = range,
            trackColor = TransparentGreenColor,
            thumbColor = PaleGreenColor,
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically)
                .padding(horizontal = Paddings.Small)
        )

        Text(
            text = "-",
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(start = Paddings.Small)
                .clickable { setThumbValuesMinusOne.value(thumbValues) }
        )
    }
}

private fun findNextFreeThumbSpot(range: IntRange, takenSpots: List<Float>): Float {
    val center = (range.first + range.last) / 2
    val maxDistance = (range.last - range.first) / 2

    for (distance in 0..maxDistance) {
        val forward = center + distance
        val backward = center - distance

        if (forward in range && takenSpots.find { it.roundToInt() == forward } == null) {
            return forward.toFloat()
        }
        if (backward in range && takenSpots.find { it.roundToInt() == backward } == null) {
            return backward.toFloat()
        }
    }
    return center.toFloat()
}

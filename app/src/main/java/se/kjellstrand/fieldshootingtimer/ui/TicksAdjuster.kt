package se.kjellstrand.fieldshootingtimer.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import se.kjellstrand.fieldshootingtimer.ui.theme.LightGreenColor
import se.kjellstrand.fieldshootingtimer.ui.theme.Paddings
import se.kjellstrand.fieldshootingtimer.ui.theme.PaleGreenColor
import se.kjellstrand.fieldshootingtimer.ui.theme.TransparentGreenColor

@Composable
fun TicksAdjuster(
    thumbValues: List<Float>,
    range: IntRange,
    enabled: Boolean = true,
    setThumbValuesMinusOne: State<() -> Unit>,
    setThumbValuesPlusOne: State<() -> Unit>,
    onHorizontalDragSetThumbValues: State<(List<Float>) -> Unit>,
    onHorizontalDragRoundThumbValues: State<() -> Unit>
) {
    Row(modifier = Modifier.padding(horizontal = Paddings.Large)) {
        Text(
            text = "+",
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(end = Paddings.Medium)
                .clickable { if (enabled) setThumbValuesPlusOne.value() }
        )
        MultiThumbSlider(
            thumbValues = thumbValues,
            onHorizontalDragSetThumbValues = onHorizontalDragSetThumbValues,
            onHorizontalDragRoundThumbValues = onHorizontalDragRoundThumbValues,
            range = range,
            enabled = enabled,
            thumbColor = PaleGreenColor,
            trackColor = LightGreenColor,
            inactiveColor = TransparentGreenColor,
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
                .padding(start = Paddings.Medium)
                .clickable { if (enabled) setThumbValuesMinusOne.value() }
        )
    }
}

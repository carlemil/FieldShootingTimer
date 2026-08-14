package se.kjellstrand.fieldshootingtimer.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import se.kjellstrand.fieldshootingtimer.ui.theme.LightGreenColor
import se.kjellstrand.fieldshootingtimer.ui.theme.Paddings
import se.kjellstrand.fieldshootingtimer.ui.theme.PaleGreenColor
import se.kjellstrand.fieldshootingtimer.ui.theme.TransparentGreenColor

internal const val TICKS_PLUS_TAG = "TicksAdjusterPlus"
internal const val TICKS_MINUS_TAG = "TicksAdjusterMinus"
internal const val TICKS_SLIDER_TAG = "TicksAdjusterSlider"

@Composable
fun TicksAdjuster(
    thumbValues: List<Float>,
    range: IntRange,
    enabled: Boolean = true,
    setThumbValuesMinusOne: () -> Unit,
    setThumbValuesPlusOne: () -> Unit,
    onHorizontalDragSetThumbValues: (List<Float>) -> Unit,
    onHorizontalDragRoundThumbValues: () -> Unit
) {
    Row(modifier = Modifier.padding(horizontal = Paddings.Large)) {
        Text(
            text = "+",
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(end = Paddings.Medium)
                .testTag(TICKS_PLUS_TAG)
                .clickable { if (enabled) setThumbValuesPlusOne() }
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
                .testTag(TICKS_SLIDER_TAG)
        )
        Text(
            text = "-",
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(start = Paddings.Medium)
                .testTag(TICKS_MINUS_TAG)
                .clickable { if (enabled) setThumbValuesMinusOne() }
        )
    }
}

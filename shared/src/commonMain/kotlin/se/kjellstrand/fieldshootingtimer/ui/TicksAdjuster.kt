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
import se.kjellstrand.fieldshootingtimer.ui.theme.Paddings

internal const val TICKS_PLUS_TAG = "TicksAdjusterPlus"
internal const val TICKS_MINUS_TAG = "TicksAdjusterMinus"

/**
 * Add/remove user ticks. Placing them on the dial and moving them is done by
 * dragging directly on the dial ring ([DialGestureOverlay]).
 */
@Composable
fun TicksAdjuster(
    enabled: Boolean = true,
    setThumbValuesMinusOne: () -> Unit,
    setThumbValuesPlusOne: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Text(
            text = "+",
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(end = Paddings.Medium)
                .testTag(TICKS_PLUS_TAG)
                .clickable { if (enabled) setThumbValuesPlusOne() }
        )
        Text(
            text = "-",
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .testTag(TICKS_MINUS_TAG)
                .clickable { if (enabled) setThumbValuesMinusOne() }
        )
    }
}

package se.kjellstrand.fieldshootingtimer.ui

import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

internal const val TICKS_PLUS_TAG = "TicksAdjusterPlus"
internal const val TICKS_MINUS_TAG = "TicksAdjusterMinus"

/**
 * One of the tick add/remove buttons. [TimerWithPlayButton] places "+" at the
 * dial's lower-left and "-" at its lower-right; placing ticks and moving them
 * is done by dragging directly on the dial ring ([DialGestureOverlay]).
 */
@Composable
internal fun TickAdjustButton(
    label: String,
    tag: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Text(
        text = label,
        fontSize = 48.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier
            .testTag(tag)
            .clickable { if (enabled) onClick() }
    )
}

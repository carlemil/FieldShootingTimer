package se.kjellstrand.fieldshootingtimer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import se.kjellstrand.fieldshootingtimer.ui.theme.BlackColor
import se.kjellstrand.fieldshootingtimer.ui.theme.WhiteColor

internal const val TICKS_PLUS_TAG = "TicksAdjusterPlus"
internal const val TICKS_MINUS_TAG = "TicksAdjusterMinus"

/**
 * One of the tick add/remove buttons, styled like the menu button (white
 * circle, black glyph). [TimerWithPlayButton] places "+" at the dial's
 * lower-left and "-" at its lower-right; placing ticks and moving them is
 * done by dragging directly on the dial ring ([DialGestureOverlay]).
 */
@Composable
internal fun TickAdjustButton(
    label: String,
    tag: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = { if (enabled) onClick() },
        modifier = modifier
            .clip(CircleShape)
            .background(WhiteColor)
            .testTag(tag)
    ) {
        Text(
            text = label,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = BlackColor
        )
    }
}

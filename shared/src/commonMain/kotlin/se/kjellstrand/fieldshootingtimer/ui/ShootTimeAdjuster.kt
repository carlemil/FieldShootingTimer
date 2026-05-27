package se.kjellstrand.fieldshootingtimer.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import se.kjellstrand.fieldshootingtimer.ui.theme.LightGreenColor
import se.kjellstrand.fieldshootingtimer.ui.theme.Paddings
import se.kjellstrand.fieldshootingtimer.ui.theme.PaleGreenColor
import se.kjellstrand.fieldshootingtimer.ui.theme.TransparentGreenColor

internal const val SHOOT_TIME_SLIDER_TAG = "ShootTimeAdjusterSlider"

@Composable
fun ShootTimeAdjuster(
    shootingDuration: Float, enabled: Boolean, onValueChange: (List<Float>) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()
    ) {
        MultiThumbSlider(
            thumbValues = listOf(shootingDuration),
            onHorizontalDragSetThumbValues = onValueChange,
            enabled = enabled,
            range = IntRange(1, 27),
            thumbColor = PaleGreenColor,
            trackColor = LightGreenColor,
            inactiveColor = TransparentGreenColor,
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically)
                .padding(start = Paddings.Large, end = Paddings.Medium)
                .testTag(SHOOT_TIME_SLIDER_TAG)
        )
        Text(
            // TODO: real R.string.shooting_time format ("Skjuttid: %d s") wired
            // back in shared/compose-resources.
            text = "Skjuttid: ${(shootingDuration + Command.CeaseFire.duration).toInt()}s",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .padding(end = Paddings.Large)
        )
    }
}

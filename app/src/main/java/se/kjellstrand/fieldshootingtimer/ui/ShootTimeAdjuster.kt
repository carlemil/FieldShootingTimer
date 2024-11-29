package se.kjellstrand.fieldshootingtimer.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import se.kjellstrand.fieldshootingtimer.R
import se.kjellstrand.fieldshootingtimer.ui.theme.LightGreenColor
import se.kjellstrand.fieldshootingtimer.ui.theme.Paddings
import se.kjellstrand.fieldshootingtimer.ui.theme.PaleGreenColor
import se.kjellstrand.fieldshootingtimer.ui.theme.TransparentGreenColor

@Composable
fun ShootTimeAdjuster(
    shootingDuration: Float, enabled: Boolean, onValueChange: State<(List<Float>) -> Unit>
) {
    val onValueChangeState by rememberUpdatedState(onValueChange)

    Row(
        verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()
    ) {

        MultiThumbSlider(
            thumbValues = listOf(shootingDuration),
            onHorizontalDragSetThumbValues = onValueChangeState,
            enabled = enabled,
            range = IntRange(1, 27),
            thumbColor = PaleGreenColor,
            trackColor = LightGreenColor,
            inactiveColor = TransparentGreenColor,
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically)
                .padding(top = Paddings.Small, start = Paddings.Large, bottom = Paddings.Large)
        )
        Text(
            text = stringResource(
                R.string.shooting_time, (shootingDuration + Command.CeaseFire.duration).toInt()
            ),
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            modifier = Modifier.padding(end = Paddings.Large)
        )
    }
}
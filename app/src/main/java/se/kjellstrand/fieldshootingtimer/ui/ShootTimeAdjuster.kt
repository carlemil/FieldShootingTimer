package se.kjellstrand.fieldshootingtimer.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import se.kjellstrand.fieldshootingtimer.R
import se.kjellstrand.fieldshootingtimer.ui.theme.LightGreenColor
import se.kjellstrand.fieldshootingtimer.ui.theme.Paddings
import se.kjellstrand.fieldshootingtimer.ui.theme.PaleGreenColor
import se.kjellstrand.fieldshootingtimer.ui.theme.TransparentGreenColor

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
        )
        Text(
            text = stringResource(
                R.string.shooting_time, (shootingDuration + Command.CeaseFire.duration).toInt()
            ),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .padding(end = Paddings.Large)
        )
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 100)
@Composable
fun ShootTimeAdjusterPreview() {
    ShootTimeAdjuster(
        shootingDuration = 15f,
        enabled = true,
        onValueChange = {}
    )
}
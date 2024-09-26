package se.kjellstrand.fieldshootingtimer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import se.kjellstrand.fieldshootingtimer.ui.theme.FieldShootingTimerTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun CenteredSemiCircleWithMarkers() {
    val semiCircleColors = SemiCircleColors(
        segmentColors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
            MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
            Color.Red,
            Color.Blue,
            Color.Green
        )
    )
    val sweepAngles = listOf(60f, 50f, 40f, 90f, 10f, 80f)

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        SegmentedSemiCircleWithMarkers(
            semiCircleColors = semiCircleColors,
            sweepAngles = sweepAngles,
            gapAngleDegrees = 30f,
            ringThickness = 20.dp,
            borderColor = Color.Black,
            borderWidth = 2.dp,
            size = 200.dp,
            markerRadius = 10.dp
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CenteredSemiCircleWithMarkersPreview() {
    FieldShootingTimerTheme {
        CenteredSemiCircleWithMarkers()
    }
}
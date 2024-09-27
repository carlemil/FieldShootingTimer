package se.kjellstrand.fieldshootingtimer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import se.kjellstrand.fieldshootingtimer.ui.theme.FieldShootingTimerTheme

@Composable
fun Timer(
    modifier: Modifier = Modifier,
    currentTime: Float = 0f,
    dialColors: DialColors = DialColors(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
            MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
            Color.Red,
            Color.Blue,
            Color.Green
        )
    ),
    gapAngleDegrees: Float = 30f,
    timesForSegments: List<Float> = listOf(6f, 5f, 40f, 90f, 10f, 80f),
    ringThickness: Dp = 20.dp,
    borderColor: Color = Color.Black,
    borderWidth: Dp = 2.dp,
    size: Dp = 200.dp,
    badgeRadius: Dp = 10.dp,
    handColor: Color = Color.White,
    handThickness: Dp = 8.dp
) {
    val totalSeconds = timesForSegments.sum()
    val availableAngle = 360f - gapAngleDegrees

    require(totalSeconds > 0) {
        "Total time must be greater than 0."
    }

    val scalingFactor = availableAngle / totalSeconds
    val sweepAngles = timesForSegments.map { it * scalingFactor }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size)
    ) {
        DialWithBadges(
            dialColors = dialColors,
            sweepAngles = sweepAngles,
            gapAngleDegrees = gapAngleDegrees,
            ringThickness = ringThickness,
            borderColor = borderColor,
            borderWidth = borderWidth,
            size = size,
            badgeRadius = badgeRadius
        )

        DialHand(
            currentTime = currentTime,
            totalTime = totalSeconds,
            availableAngle = availableAngle,
            gapAngleDegrees = gapAngleDegrees,
            size = size,
            ringThickness = ringThickness,
            borderWidth = borderWidth,
            handColor = handColor,
            borderColor = borderColor,
            handThickness = handThickness,
            overshootPercent = 0.1f // 10% overshoot
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DialWithHandPreview() {
    FieldShootingTimerTheme {
        val timeInSecondsForEachSegment = listOf(5f, 25f, 20f, 60f, 10f, 80f)
        val currentTime = 00f

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Timer(
                currentTime = currentTime,
                dialColors = DialColors(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                        Color.Red,
                        Color.Blue,
                        Color.Green
                    )
                ),
                gapAngleDegrees = 30f,
                timesForSegments = timeInSecondsForEachSegment,
                ringThickness = 30.dp,
                borderColor = Color.Black,
                borderWidth = 2.dp,
                size = 300.dp,
                badgeRadius = 15.dp,
                handColor = Color.White,
                handThickness = 4.dp
            )
        }
    }
}
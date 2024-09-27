package se.kjellstrand.fieldshootingtimer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import se.kjellstrand.fieldshootingtimer.ui.theme.FieldShootingTimerTheme
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun DialWithHand(
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
    handColor: Color = Color.Black,
    handThickness: Dp = 3.dp
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
        modifier = modifier
    ) {
        // Draw the segmented semi-circle with markers
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

        // Draw the hand
        Canvas(modifier = Modifier.size(size)) {
            val canvasSize = size.toPx()
            val ringThicknessPx = ringThickness.toPx()
            val borderWidthPx = borderWidth.toPx()
            val totalPadding = (ringThicknessPx / 2) + (borderWidthPx / 2)

            val centerX = canvasSize / 2
            val centerY = canvasSize / 2

            val arcRadius = (canvasSize / 2) - totalPadding
            val badgeCenterRadius = arcRadius + (ringThicknessPx / 2)

            val startAngle = 270f - (availableAngle / 2)
            val anglePerSecond = availableAngle / totalSeconds

            val clampedCurrentTime = currentTime.coerceIn(0f, totalSeconds)
            val handAngle = startAngle + (clampedCurrentTime * anglePerSecond)
            val angleRad = Math.toRadians(handAngle.toDouble())
            val handLength = badgeCenterRadius

            val handX = centerX + (handLength * cos(angleRad)).toFloat()
            val handY = centerY + (handLength * sin(angleRad)).toFloat()

            drawLine(
                color = handColor,
                start = Offset(centerX, centerY),
                end = Offset(handX, handY),
                strokeWidth = handThickness.toPx(),
                cap = Stroke.DefaultCap
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DialWithHandPreview() {
    FieldShootingTimerTheme {
        val timeInSecondsForEachSegment = listOf(6f, 5f, 40f, 90f, 10f, 80f)
        val totalTime = timeInSecondsForEachSegment.sum()
        val currentTime = totalTime / 4

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            DialWithHand(
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
                handColor = Color.Black,
                handThickness = 4.dp
            )
        }
    }
}
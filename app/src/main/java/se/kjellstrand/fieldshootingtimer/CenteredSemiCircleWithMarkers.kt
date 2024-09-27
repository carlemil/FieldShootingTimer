package se.kjellstrand.fieldshootingtimer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import se.kjellstrand.fieldshootingtimer.ui.theme.FieldShootingTimerTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun DialWithHand(
    currentTime: Float = 0f, // Current time to position the hand
    modifier: Modifier = Modifier,
    semiCircleColors: SemiCircleColors = SemiCircleColors(
        segmentColors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
            MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
            Color.Red,
            Color.Blue,
            Color.Green
        )
    ),
    gapAngleDegrees: Float = 30f,
    timeInSecondsForEachSegment: List<Float> = listOf(6f, 5f, 40f, 90f, 10f, 80f),
    ringThickness: Dp = 20.dp,
    borderColor: Color = Color.Black,
    borderWidth: Dp = 2.dp,
    size: Dp = 200.dp,
    markerRadius: Dp = 10.dp,
    handColor: Color = Color.Black,
    handThickness: Dp = 3.dp
) {
    val totalTime = timeInSecondsForEachSegment.sum()
    val availableAngle = 360f - gapAngleDegrees

    require(totalTime > 0) {
        "Total time must be greater than 0."
    }

    val scalingFactor = availableAngle / totalTime
    val sweepAngles = timeInSecondsForEachSegment.map { it * scalingFactor }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        // Draw the segmented semi-circle with markers
        SegmentedSemiCircleWithMarkers(
            semiCircleColors = semiCircleColors,
            sweepAngles = sweepAngles,
            gapAngleDegrees = gapAngleDegrees,
            ringThickness = ringThickness,
            borderColor = borderColor,
            borderWidth = borderWidth,
            size = size,
            markerRadius = markerRadius
        )

        // Draw the hand
        Canvas(modifier = Modifier.size(size)) {
            val canvasSize = size.toPx()
            val ringThicknessPx = ringThickness.toPx()
            val borderWidthPx = borderWidth.toPx()
            val markerRadiusPx = markerRadius.toPx()
            val totalPadding = (ringThicknessPx / 2) + (borderWidthPx / 2)

            val centerX = canvasSize / 2
            val centerY = canvasSize / 2

            val arcRadius = (canvasSize / 2) - totalPadding
            val markerCenterRadius = arcRadius + (ringThicknessPx / 2)

            // Calculate the start angle
            val startAngle = 270f - (availableAngle / 2)

            // Calculate the angle per second
            val anglePerSecond = availableAngle / totalTime

            // Calculate the current hand angle based on currentTime
            val clampedCurrentTime = currentTime.coerceIn(0f, totalTime)
            val handAngle = startAngle + (clampedCurrentTime * anglePerSecond)

            // Convert angle to radians for calculation
            val angleRad = Math.toRadians(handAngle.toDouble())

            // Calculate hand length (slightly shorter than the marker center radius)
            val handLength = markerCenterRadius

            // Calculate end point of the hand
            val handX = centerX + (handLength * cos(angleRad)).toFloat()
            val handY = centerY + (handLength * sin(angleRad)).toFloat()

            // Draw the hand
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
        // Example usage with currentTime set to half of totalTime
        val timeInSecondsForEachSegment = listOf(6f, 5f, 40f, 90f, 10f, 80f)
        val totalTime = timeInSecondsForEachSegment.sum()
        val currentTime = totalTime / 4 // Adjust this value to see different hand positions

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            DialWithHand(
                currentTime = currentTime,
                semiCircleColors = SemiCircleColors(
                    segmentColors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                        Color.Red,
                        Color.Blue,
                        Color.Green
                    )
                ),
                gapAngleDegrees = 30f,
                timeInSecondsForEachSegment = timeInSecondsForEachSegment,
                ringThickness = 30.dp,
                borderColor = Color.Black,
                borderWidth = 2.dp,
                size = 300.dp,
                markerRadius = 15.dp,
                handColor = Color.Black,
                handThickness = 4.dp
            )
        }
    }
}
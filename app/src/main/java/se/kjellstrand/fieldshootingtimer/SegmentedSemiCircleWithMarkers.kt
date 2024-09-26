// File: SegmentedSemiCircleWithMarkers.kt
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

@Composable
fun SegmentedSemiCircleWithMarkers(
    modifier: Modifier = Modifier,
    semiCircleColors: SemiCircleColors,
    sweepAngles: List<Float>,
    gapAngleDegrees: Float = 30f,
    ringThickness: Dp = 20.dp,
    borderColor: Color = Color.Black,
    borderWidth: Dp = 2.dp,
    size: Dp = 200.dp,
    markerRadius: Dp = 10.dp
) {
    Box(modifier = modifier) {
        // Draw the segmented semi-circle
        SegmentedSemiCircle(
            semiCircleColors = semiCircleColors,
            sweepAngles = sweepAngles,
            gapAngleDegrees = gapAngleDegrees,
            ringThickness = ringThickness,
            borderColor = borderColor,
            borderWidth = borderWidth,
            size = size
        )

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

            val availableAngle = 360f - gapAngleDegrees
            var currentAngle = 270f - (availableAngle / 2)

            val markerAngles = sweepAngles.map { sweep ->
                currentAngle += sweep
                currentAngle % 360
            }

            markerAngles.forEach { angle ->
                val angleRad = Math.toRadians(angle.toDouble())
                val x = centerX + (markerCenterRadius) * kotlin.math.cos(angleRad).toFloat()
                val y = centerY + (markerCenterRadius) * kotlin.math.sin(angleRad).toFloat()

                drawCircle(
                    color = Color.White,
                    radius = markerRadiusPx - (borderWidthPx / 2),
                    center = Offset(x, y)
                )

                drawCircle(
                    color = borderColor,
                    radius = markerRadiusPx,
                    center = Offset(x, y),
                    style = Stroke(width = borderWidthPx)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SegmentedSemiCircleWithMarkersPreview() {
    FieldShootingTimerTheme {
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
        val gapAngleDegrees = 30f
        val secondsForSegment = listOf(6f, 5f, 40f, 90f, 10f, 80f)
        val totalTime = secondsForSegment.sum()
        val availableAngle = 360f - gapAngleDegrees

        require(totalTime > 0) {
            "Total time must be greater than 0."
        }

        val scalingFactor = availableAngle / totalTime
        val scaledSecondsForSegment = secondsForSegment.map { it * scalingFactor }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            SegmentedSemiCircleWithMarkers(
                semiCircleColors = semiCircleColors,
                sweepAngles = scaledSecondsForSegment,
                gapAngleDegrees = gapAngleDegrees,
                ringThickness = 30.dp,
                borderColor = Color.Black,
                borderWidth = 2.dp,
                size = 300.dp,
                markerRadius = 15.dp
            )
        }
    }
}
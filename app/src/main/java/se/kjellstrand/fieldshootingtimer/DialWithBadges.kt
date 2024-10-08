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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import se.kjellstrand.fieldshootingtimer.ui.theme.FieldShootingTimerTheme
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun DialWithBadges(
    modifier: Modifier = Modifier,
    dialColors: DialColors,
    sweepAngles: List<Float>,
    timesForSegments: List<Float>,
    gapAngleDegrees: Float = 30f,
    ringThickness: Dp = 20.dp,
    borderColor: Color = Color.Black,
    borderWidth: Dp = 2.dp,
    size: Dp = 200.dp,
    badgeRadius: Dp = 10.dp
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size)
    ) {
        Dial(
            dialColors = dialColors,
            sweepAngles = sweepAngles,
            gapAngleDegrees = gapAngleDegrees,
            ringThickness = ringThickness,
            borderColor = borderColor,
            borderWidth = borderWidth,
            size = size
        )

        Badges(
            size = size,
            sweepAngles = sweepAngles,
            timesForSegments = timesForSegments,
            gapAngleDegrees = gapAngleDegrees,
            ringThickness = ringThickness,
            borderColor = borderColor,
            borderWidth = borderWidth,
            badgeRadius = badgeRadius
        )
    }
}

@Composable
fun Badges(
    size: Dp,
    sweepAngles: List<Float>,
    timesForSegments: List<Float>,
    gapAngleDegrees: Float,
    ringThickness: Dp,
    borderColor: Color,
    borderWidth: Dp,
    badgeRadius: Dp
) {
    Canvas(modifier = Modifier.size(size)) {
        val canvasSize = size.toPx()
        val ringThicknessPx = ringThickness.toPx()
        val borderWidthPx = borderWidth.toPx()
        val markerRadiusPx = badgeRadius.toPx()
        val totalPadding = (ringThicknessPx / 2) + (borderWidthPx / 2)

        val centerX = canvasSize / 2
        val centerY = canvasSize / 2

        val arcRadius = (canvasSize / 2) - totalPadding
        val markerCenterRadius = arcRadius + (ringThicknessPx / 2)

        val adjustedMarkers = calculateAdjustedMarkerAngles(
            sweepAngles = sweepAngles,
            gapAngleDegrees = gapAngleDegrees,
            badgeRadiusPx = markerRadiusPx,
            markerCenterRadius = markerCenterRadius
        )

        adjustedMarkers.zip(timesForSegments).forEach { (angle, time) ->
            val angleRad = Math.toRadians(angle.toDouble())
            val x = centerX + markerCenterRadius * cos(angleRad).toFloat()
            val y = centerY + markerCenterRadius * sin(angleRad).toFloat()

            drawBadge(
                x = x,
                y = y,
                markerRadiusPx = markerRadiusPx,
                borderWidthPx = borderWidthPx,
                borderColor = borderColor,
                timeText = time.toInt().toString()
            )
        }
    }
}

fun calculateAdjustedMarkerAngles(
    sweepAngles: List<Float>,
    gapAngleDegrees: Float,
    badgeRadiusPx: Float,
    markerCenterRadius: Float
): List<Float> {
    val availableAngle = 360f - gapAngleDegrees
    var currentAngle = 270f - (availableAngle / 2)

    val initialMarkerAngles = sweepAngles.map { sweep ->
        currentAngle += sweep / 2
        val markerAngle = currentAngle % 360
        currentAngle += sweep / 2
        markerAngle
    }

    val minAngleBetweenMarkersRadians = 2 * asin(badgeRadiusPx / markerCenterRadius)
    val minAngleBetweenMarkers = minAngleBetweenMarkersRadians * (180f / PI.toFloat())

    val sortedMarkers = initialMarkerAngles.sorted()
    val adjustedMarkers = mutableListOf<Float>()
    var previousAngle = sortedMarkers.first() - 360f

    for (angle in sortedMarkers) {
        var adjustedAngle = angle
        var angleDifference = (adjustedAngle - previousAngle) % 360f
        if (angleDifference < 0) angleDifference += 360f

        if (angleDifference < minAngleBetweenMarkers) {
            adjustedAngle = previousAngle + minAngleBetweenMarkers
            if (adjustedAngle >= 360f) adjustedAngle -= 360f
        }

        adjustedMarkers.add(adjustedAngle)
        previousAngle = adjustedAngle
    }

    return adjustedMarkers
}

fun DrawScope.drawBadge(
    x: Float,
    y: Float,
    markerRadiusPx: Float,
    borderWidthPx: Float,
    borderColor: Color,
    timeText: String
) {
    // Draw the badge circle
    drawCircle(
        color = Color.White,
        radius = markerRadiusPx - (borderWidthPx / 2),
        center = Offset(x, y)
    )

    // Draw the badge border
    drawCircle(
        color = borderColor,
        radius = markerRadiusPx,
        center = Offset(x, y),
        style = Stroke(width = borderWidthPx)
    )

    // Draw the text inside the badge
    drawContext.canvas.nativeCanvas.apply {
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = markerRadiusPx * 1.2f
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }

        val textBounds = android.graphics.Rect()
        paint.getTextBounds(timeText, 0, timeText.length, textBounds)
        val textHeight = textBounds.height()

        drawText(
            timeText,
            x,
            y + textHeight / 2f,
            paint
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SegmentedSemiCircleWithMarkersPreview() {
    FieldShootingTimerTheme {
        val semiCircleColors = DialColors(
            colors = listOf(
                MaterialTheme.colorScheme.primary,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                Color.Red,
                Color.Blue,
                Color.Green
            )
        )
        val gapAngleDegrees = 30f
        val secondsForSegment = listOf(6f, 5f, 7f, 9f, 10f, 1f)
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
            DialWithBadges(
                dialColors = semiCircleColors,
                sweepAngles = scaledSecondsForSegment,
                gapAngleDegrees = gapAngleDegrees,
                timesForSegments = listOf(6f, 5f, 40f, 90f, 10f, 80f),
                ringThickness = 30.dp,
                borderColor = Color.Black,
                borderWidth = 2.dp,
                size = 300.dp,
                badgeRadius = 15.dp
            )
        }
    }
}
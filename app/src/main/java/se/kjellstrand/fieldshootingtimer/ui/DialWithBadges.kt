package se.kjellstrand.fieldshootingtimer.ui

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
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun DialWithBadges(
    modifier: Modifier = Modifier,
    dialColors: DialColors,
    segments: List<Float>,
    ticks: List<Int>,
    availableAngle: Float,
    gapAngleDegrees: Float = 30f,
    ringThickness: Dp = 20.dp,
    borderColor: Color = Color.Black,
    borderWidth: Dp = 2.dp,
    size: Dp = 200.dp,
    badgeRadius: Dp = 10.dp,
    badgesVisible: Boolean = true
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size)
    ) {
        val sumOfSegments = segments.sum()
        val scalingFactor = availableAngle / sumOfSegments
        val sweepAngles = segments.map { it * scalingFactor }

        Dial(
            dialColors = dialColors,
            sweepAngles = sweepAngles,
            gapAngleDegrees = gapAngleDegrees,
            ringThickness = ringThickness,
            borderColor = borderColor,
            borderWidth = borderWidth,
            size = size
        )

        Dividers(
            size = size,
            sweepAngles = sweepAngles,
            gapAngleDegrees = gapAngleDegrees,
            ringThickness = ringThickness,
            borderWidth = borderWidth,
            borderColor = borderColor
        )

        TickMarks(
            size = size,
            ticks = ticks,
            ticksMax = sumOfSegments.toInt(),
            gapAngleDegrees = gapAngleDegrees,
            ringThickness = ringThickness,
            borderWidth = borderWidth,
            borderColor = borderColor
        )

        if (badgesVisible) {
            Badges(
                size = size,
                sweepAngles = sweepAngles,
                timesForSegments = segments,
                gapAngleDegrees = gapAngleDegrees,
                ringThickness = ringThickness,
                borderColor = borderColor,
                borderWidth = borderWidth,
                badgeRadius = badgeRadius
            )
        }
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

        val adjustedMarkers = centerOnSegmentMarkerAngles(
            sweepAngles = sweepAngles,
            gapAngleDegrees = gapAngleDegrees
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

@Composable
fun Dividers(
    size: Dp,
    sweepAngles: List<Float>,
    gapAngleDegrees: Float,
    ringThickness: Dp,
    borderWidth: Dp,
    borderColor: Color
) {
    Canvas(modifier = Modifier.size(size)) {
        val canvasSize = size.toPx()
        val ringThicknessPx = ringThickness.toPx()
        val borderWidthPx = borderWidth.toPx()

        val centerX = canvasSize / 2
        val centerY = canvasSize / 2

        val innerRadius = (canvasSize / 2) - ringThicknessPx - borderWidthPx
        val outerRadius = (canvasSize / 2)

        val segmentAngles = calculateSegmentAngles(
            sweepAngles = sweepAngles,
            gapAngleDegrees = gapAngleDegrees
        )

        segmentAngles.forEach { angle ->
            val angleRad = Math.toRadians(angle.toDouble())

            val startX = centerX + innerRadius * cos(angleRad).toFloat()
            val startY = centerY + innerRadius * sin(angleRad).toFloat()

            val endX = centerX + outerRadius * cos(angleRad).toFloat()
            val endY = centerY + outerRadius * sin(angleRad).toFloat()

            drawLine(
                color = borderColor,
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = borderWidthPx
            )
        }
    }
}

@Composable
fun TickMarks(
    size: Dp,
    ticks: List<Int>,
    ticksMax: Int,
    gapAngleDegrees: Float,
    ringThickness: Dp,
    borderWidth: Dp,
    borderColor: Color
) {
    Canvas(modifier = Modifier.size(size)) {
        val canvasSize = size.toPx()
        val ringThicknessPx = ringThickness.toPx()
        val borderWidthPx = borderWidth.toPx()

        val centerX = canvasSize / 2
        val centerY = canvasSize / 2

        val innerRadius = (canvasSize / 2) - ringThicknessPx / 2
        val outerRadius = (canvasSize / 2)

        val adjustedTicks = ticks.map { tick ->
            270f - (360f - gapAngleDegrees) / 2 + (tick.toFloat() / ticksMax) * (360f - gapAngleDegrees)
        }

        adjustedTicks.forEach { angle ->
            val angleRad = Math.toRadians(angle.toDouble())

            val startX = centerX + innerRadius * cos(angleRad).toFloat()
            val startY = centerY + innerRadius * sin(angleRad).toFloat()

            val endX = centerX + outerRadius * cos(angleRad).toFloat()
            val endY = centerY + outerRadius * sin(angleRad).toFloat()

            drawLine(
                color = borderColor,
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = borderWidthPx * 2
            )
        }
    }
}

fun centerOnSegmentMarkerAngles(
    sweepAngles: List<Float>,
    gapAngleDegrees: Float
): List<Float> {
    val availableAngle = 360f - gapAngleDegrees
    var currentAngle = 270f - (availableAngle / 2)

    val markerAngles = sweepAngles.map { sweep ->
        currentAngle += sweep / 2
        val markerAngle = currentAngle % 360
        currentAngle += sweep / 2
        markerAngle
    }
    return markerAngles
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

fun calculateSegmentAngles(
    sweepAngles: List<Float>,
    gapAngleDegrees: Float
): List<Float> {
    val availableAngle = 360f - gapAngleDegrees
    var currentAngle = 270f - (availableAngle / 2)
    val segmentAngles = mutableListOf<Float>()

    sweepAngles.forEach { sweep ->
        // Add the starting angle of the segment
        segmentAngles.add(currentAngle % 360)
        currentAngle += sweep
    }

    // Add the end angle (which is the same as the starting angle due to wrapping)
    segmentAngles.add((currentAngle % 360))

    return segmentAngles
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

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            DialWithBadges(
                dialColors = semiCircleColors,
                segments = listOf(7f, 3f, 6f, 2f, 3f, 1f),
                availableAngle = 330f,
                gapAngleDegrees = 30f,
                ringThickness = 30.dp,
                borderColor = Color.Black,
                borderWidth = 2.dp,
                size = 300.dp,
                badgeRadius = 15.dp,
                ticks = listOf(1, 2, 3, 4, 7, 8, 9)
            )
        }
    }
}
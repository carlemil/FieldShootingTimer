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
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun DecoratedDial(
    modifier: Modifier = Modifier,
    segmentColors: List<Color>,
    segments: List<Float>,
    ticks: List<Float>,
    gapAngleDegrees: Float = 30f,
    ringThickness: Dp = 20.dp,
    borderColor: Color = Color.Black,
    borderWidth: Dp = 2.dp,
    size: Dp = 200.dp,
    badgeRadius: Dp = 10.dp,
    segmentBadgesVisible: Boolean = true
) {
    Box(
        contentAlignment = Alignment.Center, modifier = modifier.size(size)
    ) {
        val sumOfSegments = segments.sum()
        val availableAngle = 360f - gapAngleDegrees
        val scalingFactor = availableAngle / sumOfSegments
        val sweepAngles = segments.map { it * scalingFactor }
        val ticksMax = sumOfSegments.toInt()
        val everySecondTicks = (1..ticksMax).map { it.toFloat() } // Generate ticks for every second

        Dial(
            segmentColors = segmentColors,
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
            borderWidth = borderWidth / 2f,
            borderColor = borderColor
        )

        // User defined ticks showing when to flip/drop or cervices change targets.
        Ticks(
            size = size,
            ticks = ticks,
            ticksMax = ticksMax,
            gapAngleDegrees = gapAngleDegrees,
            ringThickness = ringThickness * 1.3f,
            borderWidth = borderWidth * 1.8f,
            borderColor = borderColor
        )

        // One tick for each second, but smaller that the regular user defined ticks.
        Ticks(
            size = size,
            ticks = everySecondTicks,
            ticksMax = ticksMax,
            gapAngleDegrees = gapAngleDegrees,
            ringThickness = ringThickness / 1.7f,
            borderWidth = borderWidth / 1.4f,
            borderColor = borderColor
        )

        if (segmentBadgesVisible) {
            SegmentBadges(
                size = size,
                sweepAngles = sweepAngles,
                timesForSegments = segments,
                segmentColors = segmentColors,
                gapAngleDegrees = gapAngleDegrees,
                ringThickness = ringThickness,
                borderColor = borderColor,
                borderWidth = borderWidth,
                badgeRadius = badgeRadius / 1.2f
            )
        }

        TickBadges(
            size = size,
            ticks = ticks,
            ticksMax = sumOfSegments.toInt(),
            gapAngleDegrees = gapAngleDegrees,
            ringThickness = ringThickness,
            borderColor = borderColor,
            borderWidth = borderWidth,
            badgeRadius = badgeRadius / 1.5f
        )
    }
}

@Composable
fun TickBadges(
    size: Dp,
    ticks: List<Float>,
    ticksMax: Int,
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
        val radiusPx = badgeRadius.toPx()
        val totalPadding = (ringThicknessPx / 2) + (borderWidthPx / 2)

        val centerX = canvasSize / 2
        val centerY = canvasSize / 2

        val arcRadius = (canvasSize / 2) - totalPadding
        val markerCenterRadius = arcRadius + (ringThicknessPx / 1.6f)

        val adjustedTicks = ticks.map { tick ->
            270f - (360f - gapAngleDegrees) / 2 + (tick / ticksMax) * (360f - gapAngleDegrees)
        }

        adjustedTicks.zip(ticks).forEach { (angle, tick) ->
            val angleRad = Math.toRadians(angle.toDouble())
            val x = centerX + markerCenterRadius * cos(angleRad).toFloat()
            val y = centerY + markerCenterRadius * sin(angleRad).toFloat()
            val time =
                (tick - Command.TenSecondsLeft.duration - Command.Ready.duration).roundToInt()
                    .toString()
            drawBadge(
                x = x,
                y = y,
                markerRadiusPx = radiusPx,
                borderWidthPx = borderWidthPx,
                borderColor = borderColor,
                backgroundColor = Color.White,
                angleRad = angleRad,
                timeText = time
            )
        }
    }
}

@Composable
fun SegmentBadges(
    size: Dp,
    sweepAngles: List<Float>,
    timesForSegments: List<Float>,
    segmentColors: List<Color>,
    gapAngleDegrees: Float,
    ringThickness: Dp,
    borderColor: Color,
    borderWidth: Dp,
    badgeRadius: Dp,
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
        val markerCenterRadius = arcRadius - (ringThicknessPx / 1.6f)

        val adjustedMarkers = centerOnSegmentMarkerAngles(
            sweepAngles = sweepAngles, gapAngleDegrees = gapAngleDegrees
        )

        adjustedMarkers.zip(timesForSegments).forEachIndexed { index, (angle, time) ->
            val angleRad = Math.toRadians(angle.toDouble())
            val x = centerX + markerCenterRadius * cos(angleRad).toFloat()
            val y = centerY + markerCenterRadius * sin(angleRad).toFloat()

            drawBadge(
                x = x,
                y = y,
                markerRadiusPx = markerRadiusPx,
                borderWidthPx = borderWidthPx,
                backgroundColor = segmentColors[index],
                borderColor = borderColor,
                angleRad = angleRad,
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
            sweepAngles = sweepAngles, gapAngleDegrees = gapAngleDegrees
        )

        segmentAngles.forEach { angle ->
            val angleRad = Math.toRadians(angle.toDouble())

            val startX = centerX + innerRadius * cos(angleRad).toFloat()
            val startY = centerY + innerRadius * sin(angleRad).toFloat()

            val endX = centerX + outerRadius * cos(angleRad).toFloat()
            val endY = centerY + outerRadius * sin(angleRad).toFloat()

            drawLine(
                color = borderColor.copy(alpha = 0.5f),
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = borderWidthPx
            )
        }
    }
}

@Composable
fun Ticks(
    size: Dp,
    ticks: List<Float>,
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
            270f - (360f - gapAngleDegrees) / 2 + (tick / ticksMax) * (360f - gapAngleDegrees)
        }

        adjustedTicks.forEach { angle ->
            val angleRad = Math.toRadians(angle.toDouble())

            val tipX = centerX + innerRadius * cos(angleRad).toFloat()
            val tipY = centerY + innerRadius * sin(angleRad).toFloat()

            val halfWidth = borderWidthPx / (Math.PI * 360) * 3
            val leftBaseX = centerX + outerRadius * cos(angleRad + halfWidth).toFloat()
            val leftBaseY = centerY + outerRadius * sin(angleRad + halfWidth).toFloat()

            val rightBaseX = centerX + outerRadius * cos(angleRad - halfWidth).toFloat()
            val rightBaseY = centerY + outerRadius * sin(angleRad - halfWidth).toFloat()

            val trianglePath = androidx.compose.ui.graphics.Path().apply {
                moveTo(tipX, tipY)
                lineTo(leftBaseX, leftBaseY)
                lineTo(rightBaseX, rightBaseY)
                close()
            }

            drawPath(
                path = trianglePath, color = borderColor.copy(alpha = 0.6f)
            )
        }
    }
}

fun centerOnSegmentMarkerAngles(
    sweepAngles: List<Float>, gapAngleDegrees: Float
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
    backgroundColor: Color,
    angleRad: Double,
    timeText: String
) {
    drawCircle(
        color = backgroundColor,
        radius = markerRadiusPx - (borderWidthPx / 2),
        center = Offset(x, y)
    )

    drawCircle(
        color = Color.White, radius = markerRadiusPx - (borderWidthPx * 2), center = Offset(x, y)
    )

    drawCircle(
        color = borderColor,
        radius = markerRadiusPx,
        center = Offset(x, y),
        style = Stroke(width = borderWidthPx)
    )

    drawContext.canvas.nativeCanvas.apply {
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = markerRadiusPx * 1.2f
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }

        val angleDegrees = Math.toDegrees(angleRad + Math.PI / 2).toFloat()

        val textBounds = android.graphics.Rect()
        paint.getTextBounds(timeText, 0, timeText.length, textBounds)
        val textHeight = textBounds.height()

        save()

        translate(x, y)
        rotate(angleDegrees)
        drawText(
            timeText, 0f, textHeight / 2f, paint
        )

        restore()
    }
}

fun calculateSegmentAngles(
    sweepAngles: List<Float>, gapAngleDegrees: Float
): List<Float> {
    val availableAngle = 360f - gapAngleDegrees
    var currentAngle = 270f - (availableAngle / 2)
    val segmentAngles = mutableListOf<Float>()

    sweepAngles.forEach { sweep ->
        segmentAngles.add(currentAngle % 360)
        currentAngle += sweep
    }

    segmentAngles.add((currentAngle % 360))

    return segmentAngles
}

@Preview(showBackground = true)
@Composable
fun SegmentedSemiCircleWithMarkersPreview() {
    FieldShootingTimerTheme {
        val semiCircleColors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
            MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
            Color.Red,
            Color.Blue,
            Color.Green
        )

        Box(
            contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()
        ) {
            DecoratedDial(
                segmentColors = semiCircleColors,
                segments = listOf(7f, 3f, 6f, 2f, 3f, 1f),
                gapAngleDegrees = 30f,
                ringThickness = 30.dp,
                borderColor = Color.Black,
                borderWidth = 2.dp,
                size = 300.dp,
                badgeRadius = 15.dp,
                ticks = listOf(1f, 3f, 9f, 10f, 11f, 14f, 15f, 18f, 20f, 22f)
            )
        }
    }
}
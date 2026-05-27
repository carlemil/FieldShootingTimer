package se.kjellstrand.fieldshootingtimer.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import se.kjellstrand.fieldshootingtimer.ui.theme.BlackColor
import kotlin.math.PI
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
    badgeRadius: Dp = 10.dp
) {
    Box(
        contentAlignment = Alignment.Center, modifier = modifier.size(size)
    ) {
        val sumOfSegments = segments.sum()
        val availableAngle = DialGeometry.availableAngle(gapAngleDegrees)
        val scalingFactor = availableAngle / sumOfSegments
        val sweepAngles = segments.map { it * scalingFactor }
        val ticksMax = sumOfSegments.toInt()
        val cumulativeSegments = segments.scan(0.0f) { acc, next -> acc + next }.drop(1)
        val everySecondTicks = (1..ticksMax)
            .map { it.toFloat() }
            .filter { it !in cumulativeSegments } // Don't show ticks for segments, looks bad in ui.
        val fireIdx = Command.timedCommands.indexOf(Command.Fire)
        val fireDuration = segments.getOrNull(fireIdx) ?: 0f
        val fireStart = (Command.TenSecondsLeft.duration + Command.Ready.duration).toFloat()
        val unloadStart = unloadStartSeconds(fireDuration)

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

        // User-defined ticks showing when to flip/drop or change targets.
        // Drawn as rectangles outside the ring.
        // A trailing tick at UnloadWeapon start closes out the last interval.
        TickBlocks(
            size = size,
            ticks = userTickDisplayPositions(ticks, fireStart, unloadStart),
            ticksMax = ticksMax,
            gapAngleDegrees = gapAngleDegrees,
            ringThickness = ringThickness / 1.7f,
            borderWidth = borderWidth / 1.4f,
            tickColor = BlackColor
        )

        // One tick for each second, drawn as inward-pointing triangles inside the ring.
        Ticks(
            size = size,
            ticks = everySecondTicks,
            ticksMax = ticksMax,
            gapAngleDegrees = gapAngleDegrees,
            ringThickness = ringThickness / 1.7f,
            borderWidth = borderWidth / 1.4f,
            tickColor = borderColor.copy(alpha = 0.6f)
        )

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

        TickBadges(
            size = size,
            ticks = ticks,
            ticksMax = sumOfSegments.toInt(),
            unloadStart = unloadStart,
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
    unloadStart: Float,
    gapAngleDegrees: Float,
    ringThickness: Dp,
    borderColor: Color,
    borderWidth: Dp,
    badgeRadius: Dp
) {
    val textMeasurer = rememberTextMeasurer()
    val fireStart = (Command.TenSecondsLeft.duration + Command.Ready.duration).toFloat()
    val placements = tickBadgePlacements(ticks, fireStart, unloadStart)
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

        placements.forEach { (position, delta) ->
            val angle = DialGeometry.tickAngle(position, ticksMax.toFloat(), gapAngleDegrees)
            val angleRad = angle.toDouble() * PI / 180.0
            val x = centerX + markerCenterRadius * cos(angleRad).toFloat()
            val y = centerY + markerCenterRadius * sin(angleRad).toFloat()
            drawBadge(
                x = x,
                y = y,
                markerRadiusPx = radiusPx,
                borderWidthPx = borderWidthPx,
                borderColor = borderColor,
                backgroundColor = Color.White,
                angleRad = angleRad,
                timeText = delta.toString(),
                textMeasurer = textMeasurer
            )
        }
    }
}

fun tickBadgePlacements(
    ticks: List<Float>,
    fireStart: Float,
    unloadStart: Float
): List<Pair<Float, Int>> {
    if (ticks.isEmpty()) return emptyList()
    val boundaries = listOf(fireStart) + ticks.sorted() + listOf(unloadStart)
    return boundaries.zipWithNext { a, b -> ((a + b) / 2f) to (b - a).roundToInt() }
}

fun unloadStartSeconds(fireDuration: Float): Float =
    Command.TenSecondsLeft.duration +
        Command.Ready.duration +
        fireDuration +
        Command.CeaseFire.duration

fun userTickDisplayPositions(
    ticks: List<Float>,
    fireStart: Float,
    unloadStart: Float
): List<Float> =
    if (ticks.isEmpty()) emptyList() else listOf(fireStart) + ticks + unloadStart

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
    val textMeasurer = rememberTextMeasurer()
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
            val angleRad = angle.toDouble() * PI / 180.0
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
                timeText = time.toInt().toString(),
                textMeasurer = textMeasurer
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
            val angleRad = angle.toDouble() * PI / 180.0

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
    tickColor: Color
) {
    Canvas(modifier = Modifier.size(size)) {
        val canvasSize = size.toPx()
        val ringThicknessPx = ringThickness.toPx()
        val borderWidthPx = borderWidth.toPx()

        val centerX = canvasSize / 2
        val centerY = canvasSize / 2

        val tipRadius = (canvasSize / 2) - ringThicknessPx / 2
        val baseRadius = canvasSize / 2

        val adjustedTicks = ticks.map { tick ->
            DialGeometry.tickAngle(tick, ticksMax.toFloat(), gapAngleDegrees)
        }

        adjustedTicks.forEach { angle ->
            val angleRad = angle.toDouble() * PI / 180.0
            val halfWidth = borderWidthPx / (PI * 360) * 3

            val tipX = centerX + tipRadius * cos(angleRad).toFloat()
            val tipY = centerY + tipRadius * sin(angleRad).toFloat()
            val leftBaseX = centerX + baseRadius * cos(angleRad + halfWidth).toFloat()
            val leftBaseY = centerY + baseRadius * sin(angleRad + halfWidth).toFloat()
            val rightBaseX = centerX + baseRadius * cos(angleRad - halfWidth).toFloat()
            val rightBaseY = centerY + baseRadius * sin(angleRad - halfWidth).toFloat()

            val trianglePath = androidx.compose.ui.graphics.Path().apply {
                moveTo(tipX, tipY)
                lineTo(leftBaseX, leftBaseY)
                lineTo(rightBaseX, rightBaseY)
                close()
            }

            drawPath(path = trianglePath, color = tickColor)
        }
    }
}

@Composable
fun TickBlocks(
    size: Dp,
    ticks: List<Float>,
    ticksMax: Int,
    gapAngleDegrees: Float,
    ringThickness: Dp,
    borderWidth: Dp,
    tickColor: Color
) {
    Canvas(modifier = Modifier.size(size)) {
        val canvasSize = size.toPx()
        val ringThicknessPx = ringThickness.toPx()
        val borderWidthPx = borderWidth.toPx()

        val centerX = canvasSize / 2
        val centerY = canvasSize / 2

        val innerRadius = canvasSize / 2
        val outerRadius = innerRadius + ringThicknessPx / 2

        val adjustedTicks = ticks.map { tick ->
            DialGeometry.tickAngle(tick, ticksMax.toFloat(), gapAngleDegrees)
        }

        adjustedTicks.forEach { angle ->
            val angleRad = angle.toDouble() * PI / 180.0
            val halfWidth = borderWidthPx / (PI * 360) * 3

            val innerLeftX = centerX + innerRadius * cos(angleRad + halfWidth).toFloat()
            val innerLeftY = centerY + innerRadius * sin(angleRad + halfWidth).toFloat()
            val outerLeftX = centerX + outerRadius * cos(angleRad + halfWidth).toFloat()
            val outerLeftY = centerY + outerRadius * sin(angleRad + halfWidth).toFloat()
            val outerRightX = centerX + outerRadius * cos(angleRad - halfWidth).toFloat()
            val outerRightY = centerY + outerRadius * sin(angleRad - halfWidth).toFloat()
            val innerRightX = centerX + innerRadius * cos(angleRad - halfWidth).toFloat()
            val innerRightY = centerY + innerRadius * sin(angleRad - halfWidth).toFloat()

            val rectPath = androidx.compose.ui.graphics.Path().apply {
                moveTo(innerLeftX, innerLeftY)
                lineTo(outerLeftX, outerLeftY)
                lineTo(outerRightX, outerRightY)
                lineTo(innerRightX, innerRightY)
                close()
            }

            drawPath(path = rectPath, color = tickColor)
        }
    }
}

fun centerOnSegmentMarkerAngles(
    sweepAngles: List<Float>, gapAngleDegrees: Float
): List<Float> {
    var currentAngle = DialGeometry.startAngle(gapAngleDegrees)

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
    timeText: String,
    textMeasurer: TextMeasurer
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

    val fontSizeSp = (markerRadiusPx * 1.2f).toSp()
    val layout = textMeasurer.measure(
        text = timeText,
        style = TextStyle(color = Color.Black, fontSize = fontSizeSp)
    )
    val angleDegrees = ((angleRad + PI / 2) * 180.0 / PI).toFloat()
    rotate(degrees = angleDegrees, pivot = Offset(x, y)) {
        drawText(
            textLayoutResult = layout,
            topLeft = Offset(
                x - layout.size.width / 2f,
                y - layout.size.height / 2f
            )
        )
    }
}

fun calculateSegmentAngles(
    sweepAngles: List<Float>, gapAngleDegrees: Float
): List<Float> {
    var currentAngle = DialGeometry.startAngle(gapAngleDegrees)
    val segmentAngles = mutableListOf<Float>()

    sweepAngles.forEach { sweep ->
        segmentAngles.add(currentAngle % 360)
        currentAngle += sweep
    }

    segmentAngles.add((currentAngle % 360))

    return segmentAngles
}

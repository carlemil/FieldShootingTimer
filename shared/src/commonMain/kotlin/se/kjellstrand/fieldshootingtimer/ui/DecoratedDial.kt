package se.kjellstrand.fieldshootingtimer.ui

import se.kjellstrand.fieldshootingtimer.domain.Command

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import se.kjellstrand.fieldshootingtimer.domain.fireStartSeconds
import se.kjellstrand.fieldshootingtimer.ui.theme.BlackColor
import kotlin.math.PI
import kotlin.math.roundToInt

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
        val dialSweepAngles = sweepAngles(segments, gapAngleDegrees)
        val ticksMax = segments.sum().toInt()
        val everySecondTicks = perSecondTickSeconds(segments)
        val fireDuration = segments.getOrNull(Command.fireSegmentIndex) ?: 0f
        val fireStart = fireStartSeconds()
        val unloadStart = unloadStartSeconds(fireDuration)

        Dial(
            segmentColors = segmentColors,
            sweepAngles = dialSweepAngles,
            gapAngleDegrees = gapAngleDegrees,
            ringThickness = ringThickness,
            borderColor = borderColor,
            borderWidth = borderWidth,
            size = size
        )

        Dividers(
            size = size,
            sweepAngles = dialSweepAngles,
            gapAngleDegrees = gapAngleDegrees,
            ringThickness = ringThickness,
            borderWidth = borderWidth / 2f,
            borderColor = borderColor
        )

        // User-defined ticks showing when to flip/drop or change targets.
        // Drawn as rectangles outside the ring.
        // A trailing tick at UnloadWeapon start closes out the last interval.
        TickMarks(
            size = size,
            ticks = userTickDisplayPositions(ticks, fireStart, unloadStart),
            ticksMax = ticksMax,
            gapAngleDegrees = gapAngleDegrees,
            ringThickness = ringThickness / 1.7f,
            borderWidth = borderWidth / 1.4f,
            tickColor = BlackColor,
            shape = TickShape.Block
        )

        // One tick for each second, drawn as inward-pointing triangles inside the ring.
        TickMarks(
            size = size,
            ticks = everySecondTicks,
            ticksMax = ticksMax,
            gapAngleDegrees = gapAngleDegrees,
            ringThickness = ringThickness / 1.7f,
            borderWidth = borderWidth / 1.4f,
            tickColor = borderColor.copy(alpha = 0.6f),
            shape = TickShape.Triangle
        )

        SegmentBadges(
            size = size,
            sweepAngles = dialSweepAngles,
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
            ticksMax = ticksMax,
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
internal fun TickBadges(
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
    val placements = tickBadgePlacements(ticks, fireStartSeconds(), unloadStart)
    Canvas(modifier = Modifier.size(size)) {
        val canvasSize = size.toPx()
        val borderWidthPx = borderWidth.toPx()
        val radii = dialRadii(canvasSize, ringThickness.toPx(), borderWidthPx)
        val center = Offset(canvasSize / 2, canvasSize / 2)

        placements.forEach { (position, delta) ->
            val angle = DialGeometry.tickAngle(position, ticksMax.toFloat(), gapAngleDegrees)
            val badgeCenter = polarToCartesian(center, radii.outerBadgeRadius, angle)
            drawBadge(
                center = badgeCenter,
                markerRadiusPx = badgeRadius.toPx(),
                borderWidthPx = borderWidthPx,
                borderColor = borderColor,
                backgroundColor = Color.White,
                angleDeg = angle,
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
internal fun SegmentBadges(
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
        val borderWidthPx = borderWidth.toPx()
        val radii = dialRadii(canvasSize, ringThickness.toPx(), borderWidthPx)
        val center = Offset(canvasSize / 2, canvasSize / 2)

        val adjustedMarkers = centerOnSegmentMarkerAngles(
            sweepAngles = sweepAngles, gapAngleDegrees = gapAngleDegrees
        )

        adjustedMarkers.zip(timesForSegments).forEachIndexed { index, (angle, time) ->
            drawBadge(
                center = polarToCartesian(center, radii.innerBadgeRadius, angle),
                markerRadiusPx = badgeRadius.toPx(),
                borderWidthPx = borderWidthPx,
                backgroundColor = segmentColors[index],
                borderColor = borderColor,
                angleDeg = angle,
                timeText = time.toInt().toString(),
                textMeasurer = textMeasurer
            )
        }
    }
}

@Composable
internal fun Dividers(
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
        val center = Offset(canvasSize / 2, canvasSize / 2)

        val innerRadius = (canvasSize / 2) - ringThicknessPx - borderWidthPx
        val outerRadius = (canvasSize / 2)

        val segmentAngles = calculateSegmentAngles(
            sweepAngles = sweepAngles, gapAngleDegrees = gapAngleDegrees
        )

        segmentAngles.forEach { angle ->
            drawLine(
                color = borderColor.copy(alpha = 0.5f),
                start = polarToCartesian(center, innerRadius, angle),
                end = polarToCartesian(center, outerRadius, angle),
                strokeWidth = borderWidthPx
            )
        }
    }
}

internal enum class TickShape { Triangle, Block }

/**
 * Small tick marks around the ring. [TickShape.Triangle] points inward from
 * the ring's edge (per-second ticks); [TickShape.Block] sits as a rectangle
 * outside the ring (user-placed ticks).
 */
@Composable
internal fun TickMarks(
    size: Dp,
    ticks: List<Float>,
    ticksMax: Int,
    gapAngleDegrees: Float,
    ringThickness: Dp,
    borderWidth: Dp,
    tickColor: Color,
    shape: TickShape
) {
    Canvas(modifier = Modifier.size(size)) {
        val canvasSize = size.toPx()
        val ringThicknessPx = ringThickness.toPx()
        val halfWidthRad = tickWedgeHalfWidthRadians(borderWidth.toPx())
        val halfWidthDeg = (halfWidthRad * 180.0 / PI).toFloat()
        val center = Offset(canvasSize / 2, canvasSize / 2)

        val innerRadius: Float
        val outerRadius: Float
        when (shape) {
            TickShape.Triangle -> {
                innerRadius = (canvasSize / 2) - ringThicknessPx / 2
                outerRadius = canvasSize / 2
            }

            TickShape.Block -> {
                innerRadius = canvasSize / 2
                outerRadius = innerRadius + ringThicknessPx / 2
            }
        }

        ticks.map { tick ->
            DialGeometry.tickAngle(tick, ticksMax.toFloat(), gapAngleDegrees)
        }.forEach { angle ->
            val path = when (shape) {
                TickShape.Triangle -> Path().apply {
                    val tip = polarToCartesian(center, innerRadius, angle)
                    val leftBase = polarToCartesian(center, outerRadius, angle + halfWidthDeg)
                    val rightBase = polarToCartesian(center, outerRadius, angle - halfWidthDeg)
                    moveTo(tip.x, tip.y)
                    lineTo(leftBase.x, leftBase.y)
                    lineTo(rightBase.x, rightBase.y)
                    close()
                }

                TickShape.Block -> Path().apply {
                    val innerLeft = polarToCartesian(center, innerRadius, angle + halfWidthDeg)
                    val outerLeft = polarToCartesian(center, outerRadius, angle + halfWidthDeg)
                    val outerRight = polarToCartesian(center, outerRadius, angle - halfWidthDeg)
                    val innerRight = polarToCartesian(center, innerRadius, angle - halfWidthDeg)
                    moveTo(innerLeft.x, innerLeft.y)
                    lineTo(outerLeft.x, outerLeft.y)
                    lineTo(outerRight.x, outerRight.y)
                    lineTo(innerRight.x, innerRight.y)
                    close()
                }
            }
            drawPath(path = path, color = tickColor)
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

internal fun DrawScope.drawBadge(
    center: Offset,
    markerRadiusPx: Float,
    borderWidthPx: Float,
    borderColor: Color,
    backgroundColor: Color,
    angleDeg: Float,
    timeText: String,
    textMeasurer: TextMeasurer
) {
    drawCircle(
        color = backgroundColor,
        radius = markerRadiusPx - (borderWidthPx / 2),
        center = center
    )

    drawCircle(
        color = Color.White, radius = markerRadiusPx - (borderWidthPx * 2), center = center
    )

    drawCircle(
        color = borderColor,
        radius = markerRadiusPx,
        center = center,
        style = Stroke(width = borderWidthPx)
    )

    val fontSizeSp = (markerRadiusPx * 1.2f).toSp()
    val layout = textMeasurer.measure(
        text = timeText,
        style = TextStyle(color = Color.Black, fontSize = fontSizeSp)
    )
    rotate(degrees = angleDeg + 90f, pivot = center) {
        drawText(
            textLayoutResult = layout,
            topLeft = Offset(
                center.x - layout.size.width / 2f,
                center.y - layout.size.height / 2f
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

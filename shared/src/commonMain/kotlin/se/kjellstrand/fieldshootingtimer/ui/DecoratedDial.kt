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

        // User-placed partids, drawn as small flags planted on the ring's
        // outer edge with the pennant pointing clockwise (forward in time).
        // The interval ends (Fire start / dial end) need no markers of their
        // own — the segment divider and the dial's edge already are ones.
        TickFlags(
            size = size,
            ticks = ticks,
            ticksMax = ticksMax,
            gapAngleDegrees = gapAngleDegrees,
            ringThickness = ringThickness,
            borderWidth = borderWidth,
            borderColor = BlackColor
        )

        // One tick for each second, drawn as inward-pointing triangles inside the ring.
        TickMarks(
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

/**
 * A partid marker: a pole planted on the ring's outer edge with a bordered
 * pennant at the top pointing clockwise — forward in time. Sized off the
 * ring thickness so it reaches about as far out as the interval badges.
 */
@Composable
internal fun TickFlags(
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
        val ringPx = ringThickness.toPx()
        val borderWidthPx = borderWidth.toPx()
        val center = Offset(canvasSize / 2, canvasSize / 2)

        val poleBase = canvasSize / 2 - borderWidthPx * 2
        val poleTop = canvasSize / 2 + ringPx * 0.19f
        val flagBottom = poleTop - ringPx * 0.14f
        // Pennant length as arc degrees at its radius, so flags look the same
        // regardless of dial size.
        val flagSweepDeg =
            (ringPx * 0.2f / poleTop * 180.0 / PI).toFloat()

        ticks.map { tick ->
            DialGeometry.tickAngle(tick, ticksMax.toFloat(), gapAngleDegrees)
        }.forEach { angle ->
            drawLine(
                color = borderColor,
                start = polarToCartesian(center, poleBase, angle),
                end = polarToCartesian(center, poleTop, angle),
                strokeWidth = borderWidthPx * 2f,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
            val pennant = Path().apply {
                val top = polarToCartesian(center, poleTop, angle)
                val bottom = polarToCartesian(center, flagBottom, angle)
                val tip = polarToCartesian(
                    center, (poleTop + flagBottom) / 2, angle + flagSweepDeg
                )
                moveTo(top.x, top.y)
                lineTo(tip.x, tip.y)
                lineTo(bottom.x, bottom.y)
                close()
            }
            drawPath(pennant, color = Color.White)
            drawPath(
                pennant,
                color = borderColor,
                style = Stroke(width = borderWidthPx, join = androidx.compose.ui.graphics.StrokeJoin.Round)
            )
        }
    }
}

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

/**
 * Per-second tick marks: small triangles pointing inward from the ring's
 * outer edge.
 */
@Composable
internal fun TickMarks(
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
        val halfWidthRad = tickWedgeHalfWidthRadians(borderWidth.toPx())
        val halfWidthDeg = (halfWidthRad * 180.0 / PI).toFloat()
        val center = Offset(canvasSize / 2, canvasSize / 2)

        val innerRadius = (canvasSize / 2) - ringThicknessPx / 2
        val outerRadius = canvasSize / 2

        ticks.map { tick ->
            DialGeometry.tickAngle(tick, ticksMax.toFloat(), gapAngleDegrees)
        }.forEach { angle ->
            val path = Path().apply {
                val tip = polarToCartesian(center, innerRadius, angle)
                val leftBase = polarToCartesian(center, outerRadius, angle + halfWidthDeg)
                val rightBase = polarToCartesian(center, outerRadius, angle - halfWidthDeg)
                moveTo(tip.x, tip.y)
                lineTo(leftBase.x, leftBase.y)
                lineTo(rightBase.x, rightBase.y)
                close()
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

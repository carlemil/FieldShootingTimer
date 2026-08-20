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
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import se.kjellstrand.fieldshootingtimer.domain.boundaryFlagSeconds
import se.kjellstrand.fieldshootingtimer.domain.fireStartSeconds
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
        // As soon as any user flag exists, the interval ends (Fire start /
        // dial end) get immovable flags of the same look — the gesture
        // overlay only ever grabs ticks from the user list, so these can't
        // be dragged. onBackground, not BlackColor: the flags stand on the
        // screen background, and black poles vanish against the dark theme
        // (1.46:1).
        TickFlags(
            size = size,
            ticks = ticks + boundaryFlagSeconds(ticks, fireDuration),
            ticksMax = ticksMax,
            gapAngleDegrees = gapAngleDegrees,
            ringThickness = ringThickness,
            borderWidth = borderWidth,
            borderColor = MaterialTheme.colorScheme.outlineVariant,
            pennantColor = MaterialTheme.colorScheme.surfaceBright
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
    val innerColor = MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onSurface
    Canvas(modifier = Modifier.size(size)) {
        val canvasSize = size.toPx()
        val borderWidthPx = borderWidth.toPx()
        val radii = dialRadii(canvasSize, ringThickness.toPx(), borderWidthPx)
        val center = Offset(canvasSize / 2, canvasSize / 2)

        placements.forEach { (position, delta) ->
            // Skip badges whose interval is squeezed too narrow to hold them
            // (extreme Fire durations shrink neighboring intervals to slivers).
            val intervalSweepDeg =
                delta / ticksMax.toFloat() * DialGeometry.availableAngle(gapAngleDegrees)
            if (!badgeFitsInSweep(intervalSweepDeg, badgeRadius.toPx(), radii.outerBadgeRadius)) {
                return@forEach
            }
            val angle = DialGeometry.tickAngle(position, ticksMax.toFloat(), gapAngleDegrees)
            val badgeCenter = polarToCartesian(center, radii.outerBadgeRadius, angle)
            drawBadge(
                center = badgeCenter,
                markerRadiusPx = badgeRadius.toPx(),
                borderWidthPx = borderWidthPx,
                borderColor = borderColor,
                backgroundColor = innerColor,
                innerColor = innerColor,
                textColor = textColor,
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
    borderColor: Color,
    pennantColor: Color
) {
    Canvas(modifier = Modifier.size(size)) {
        val canvasSize = size.toPx()
        val ringPx = ringThickness.toPx()
        val borderWidthPx = borderWidth.toPx()
        val center = Offset(canvasSize / 2, canvasSize / 2)

        // The pole starts exactly at the dial border's outer edge (canvas
        // radius), so the whole flag sits outside the dial face.
        val poleBase = canvasSize / 2
        val poleTop = canvasSize / 2 + ringPx * 0.26f
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
            drawPath(pennant, color = pennantColor)
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
    val innerColor = MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onSurface
    Canvas(modifier = Modifier.size(size)) {
        val canvasSize = size.toPx()
        val borderWidthPx = borderWidth.toPx()
        val radii = dialRadii(canvasSize, ringThickness.toPx(), borderWidthPx)
        val center = Offset(canvasSize / 2, canvasSize / 2)

        val adjustedMarkers = centerOnSegmentMarkerAngles(
            sweepAngles = sweepAngles, gapAngleDegrees = gapAngleDegrees
        )

        adjustedMarkers.zip(timesForSegments).forEachIndexed { index, (angle, time) ->
            // A segment squeezed into a sliver (extreme Fire durations) can't
            // hold its badge — drop it rather than let neighbors collide.
            if (!badgeFitsInSweep(sweepAngles[index], badgeRadius.toPx(), radii.innerBadgeRadius)) {
                return@forEachIndexed
            }
            drawBadge(
                center = polarToCartesian(center, radii.innerBadgeRadius, angle),
                markerRadiusPx = badgeRadius.toPx(),
                borderWidthPx = borderWidthPx,
                backgroundColor = segmentColors[index],
                borderColor = borderColor,
                innerColor = innerColor,
                textColor = textColor,
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
    // Themed (surface/onSurface) so badges stop being the dark theme's only
    // bright-white elements; light theme keeps its white-and-black look.
    innerColor: Color,
    textColor: Color,
    timeText: String,
    textMeasurer: TextMeasurer
) {
    drawCircle(
        color = backgroundColor,
        radius = markerRadiusPx - (borderWidthPx / 2),
        center = center
    )

    drawCircle(
        color = innerColor, radius = markerRadiusPx - (borderWidthPx * 2), center = center
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
        style = TextStyle(color = textColor, fontSize = fontSizeSp)
    )
    // Screen-upright, deliberately unrotated: digits rotated to follow the
    // dial read sideways at 3 o'clock and upside down along the bottom.
    drawText(
        textLayoutResult = layout,
        topLeft = Offset(
            center.x - layout.size.width / 2f,
            center.y - layout.size.height / 2f
        )
    )
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

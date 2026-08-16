package se.kjellstrand.fieldshootingtimer.ui

import androidx.compose.ui.geometry.Offset
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

object DialGeometry {
    const val TOP_ANGLE_DEG = 270f
    const val FULL_CIRCLE_DEG = 360f

    fun availableAngle(gapDegrees: Float): Float = FULL_CIRCLE_DEG - gapDegrees

    fun startAngle(gapDegrees: Float): Float = TOP_ANGLE_DEG - availableAngle(gapDegrees) / 2

    fun tickAngle(tick: Float, ticksMax: Float, gapDegrees: Float): Float {
        val avail = availableAngle(gapDegrees)
        return TOP_ANGLE_DEG - avail / 2 + (tick / ticksMax) * avail
    }
}

/** Sweep angle of each segment, proportional to its share of the total time. */
internal fun sweepAngles(segments: List<Float>, gapAngleDegrees: Float): List<Float> {
    val total = segments.sum()
    if (total <= 0f) return segments.map { 0f }
    val scalingFactor = DialGeometry.availableAngle(gapAngleDegrees) / total
    return segments.map { it * scalingFactor }
}

/**
 * Seconds between small dial ticks: 1 while everything fits, else the
 * smallest round step keeping the tick count readable (at most ~60 ticks —
 * needed since the Fire segment can stretch the dial to minutes).
 */
internal fun tickStepSeconds(totalSeconds: Float): Int =
    listOf(1, 5, 10, 15, 30, 60).firstOrNull { totalSeconds / it <= 60f } ?: 60

/**
 * The whole seconds that get a small dial tick: every [tickStepSeconds]
 * multiple up to the total, except those sitting on a segment boundary (a
 * divider is already drawn there). Boundary matching is epsilon-based so
 * accumulated float error — or fractional segment durations — can't leak
 * boundary ticks.
 */
internal fun perSecondTickSeconds(segments: List<Float>, epsilon: Float = 1e-3f): List<Float> {
    val boundaries = segments.scan(0f) { acc, next -> acc + next }.drop(1)
    val step = tickStepSeconds(segments.sum())
    return (1..segments.sum().toInt() / step)
        .map { (it * step).toFloat() }
        .filter { second -> boundaries.none { abs(it - second) <= epsilon } }
}

/** A point at [radius] from [center] in the direction of [angleDeg]. */
internal fun polarToCartesian(center: Offset, radius: Float, angleDeg: Float): Offset {
    val angleRad = angleDeg.toDouble() * PI / 180.0
    return Offset(
        center.x + radius * cos(angleRad).toFloat(),
        center.y + radius * sin(angleRad).toFloat()
    )
}

/** Half-width of a tick wedge in radians, scaled from the border width. */
internal fun tickWedgeHalfWidthRadians(borderWidthPx: Float): Float =
    (borderWidthPx / (PI * 360) * 3).toFloat()

/** Angle of [position] as seen from [center], in degrees normalized to [0, 360). */
internal fun angleFromCenterDegrees(center: Offset, position: Offset): Float {
    val deg = atan2(
        (position.y - center.y).toDouble(),
        (position.x - center.x).toDouble()
    ) * 180.0 / PI
    return (((deg % 360.0) + 360.0) % 360.0).toFloat()
}

/**
 * Inverse of [DialGeometry.tickAngle]: the tick value whose marker sits at
 * [angleDeg]. Angles inside the bottom gap snap to the nearer end (0 or
 * [ticksMax]), so a finger sliding past the dial's end doesn't wrap around.
 */
internal fun dialAngleToTickValue(angleDeg: Float, ticksMax: Float, gapDegrees: Float): Float {
    val avail = DialGeometry.availableAngle(gapDegrees)
    var rel = (((angleDeg - DialGeometry.startAngle(gapDegrees)) % 360f) + 360f) % 360f
    if (rel > avail) {
        rel = if (rel - avail <= 360f - rel) avail else 0f
    }
    return rel / avail * ticksMax
}

/** Index of the tick in [ticks] nearest [value], or null if none is within [toleranceSeconds]. */
internal fun nearestTickIndex(value: Float, ticks: List<Float>, toleranceSeconds: Float): Int? =
    ticks.withIndex()
        .minByOrNull { abs(it.value - value) }
        ?.takeIf { abs(it.value - value) <= toleranceSeconds }
        ?.index

/**
 * True when a touch at [distanceFromCenterPx] falls on the draggable ring
 * band: from half a ring inside the ring's inner edge (fat fingers) out past
 * the tick blocks that sit outside the ring. Keeps drags that start on the
 * dial face or the center play button from grabbing a tick.
 */
internal fun isWithinRingBand(
    distanceFromCenterPx: Float,
    canvasSizePx: Float,
    ringThicknessPx: Float
): Boolean {
    val outerRadius = canvasSizePx / 2f
    return distanceFromCenterPx >= outerRadius - ringThicknessPx * 1.5f &&
        distanceFromCenterPx <= outerRadius + ringThicknessPx
}

/**
 * True when [tickValue] falls inside the wedge spanning [wedgeStart]..[wedgeEnd]
 * seconds, widened by [slackSeconds] at both ends so a short wedge (e.g. a 1s
 * Fire segment) can still fit two pinching fingers.
 */
internal fun isWithinWedge(
    tickValue: Float,
    wedgeStart: Float,
    wedgeEnd: Float,
    slackSeconds: Float
): Boolean = tickValue >= wedgeStart - slackSeconds && tickValue <= wedgeEnd + slackSeconds

/**
 * Distance in px from [position] to the dial hand, modeled as the segment
 * from [center] of length [handLengthPx] toward [handAngleDeg]. Positions
 * past the hand's tip measure to the tip, so a finger can't grab the hand
 * by pointing at its extension beyond the dial.
 */
internal fun distanceToHandPx(
    center: Offset,
    position: Offset,
    handAngleDeg: Float,
    handLengthPx: Float
): Float {
    val end = polarToCartesian(center, handLengthPx, handAngleDeg)
    val segment = end - center
    val toPosition = position - center
    val segmentLengthSquared = segment.x * segment.x + segment.y * segment.y
    if (segmentLengthSquared <= 0f) return toPosition.getDistance()
    val t = ((toPosition.x * segment.x + toPosition.y * segment.y) / segmentLengthSquared)
        .coerceIn(0f, 1f)
    val projection = Offset(center.x + segment.x * t, center.y + segment.y * t)
    return (position - projection).getDistance()
}

/**
 * Seconds of tick-value tolerance corresponding to [touchSlopPx] of arc length
 * at [arcRadiusPx] — so the grab distance feels the same regardless of how
 * many seconds the dial spans.
 */
internal fun tickDragToleranceSeconds(
    touchSlopPx: Float,
    arcRadiusPx: Float,
    ticksMax: Float,
    gapDegrees: Float
): Float {
    val availRad = DialGeometry.availableAngle(gapDegrees) * (PI.toFloat() / 180f)
    return ticksMax * touchSlopPx / (availRad * arcRadiusPx)
}

/** The radii DecoratedDial's overlays hang off, all derived from the ring. */
internal data class DialRadii(
    val arcRadius: Float,
    val outerBadgeRadius: Float,
    val innerBadgeRadius: Float
)

internal fun dialRadii(canvasSizePx: Float, ringThicknessPx: Float, borderWidthPx: Float): DialRadii {
    val totalPadding = (ringThicknessPx / 2) + (borderWidthPx / 2)
    val arcRadius = (canvasSizePx / 2) - totalPadding
    return DialRadii(
        arcRadius = arcRadius,
        outerBadgeRadius = arcRadius + (ringThicknessPx / 1.6f),
        innerBadgeRadius = arcRadius - (ringThicknessPx / 1.6f)
    )
}

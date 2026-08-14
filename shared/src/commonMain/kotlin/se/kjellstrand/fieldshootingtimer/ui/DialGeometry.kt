package se.kjellstrand.fieldshootingtimer.ui

import androidx.compose.ui.geometry.Offset
import kotlin.math.PI
import kotlin.math.abs
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
 * The whole seconds that get a small per-second tick: every integer second up
 * to the total, except those sitting on a segment boundary (a divider is
 * already drawn there). Boundary matching is epsilon-based so accumulated
 * float error — or fractional segment durations — can't leak boundary ticks.
 */
internal fun perSecondTickSeconds(segments: List<Float>, epsilon: Float = 1e-3f): List<Float> {
    val boundaries = segments.scan(0f) { acc, next -> acc + next }.drop(1)
    return (1..segments.sum().toInt())
        .map { it.toFloat() }
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

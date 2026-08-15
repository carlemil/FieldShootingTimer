package se.kjellstrand.fieldshootingtimer.ui

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.roundToInt

// Invisible gesture surface covering the dial; tagged for UI tests.
internal const val DIAL_GESTURE_TAG = "DialGestureSurface"

// Allowed shooting (Fire) duration in seconds, adjusted by pinching the
// Fire segment.
internal const val SHOOT_TIME_MIN = 1
internal const val SHOOT_TIME_MAX = 300

/** How much arc a finger may miss a tick block by and still grab it. */
private val TickTouchSlop = 24.dp

/** How far outside the Fire wedge (in seconds) a pinch finger may start. */
private const val PinchWedgeSlackSeconds = 2f

/**
 * Transparent overlay for touch gestures on the dial ring:
 *
 * - **One finger** starting on the ring near a user tick grabs the nearest
 *   tick and follows the finger's angle around the dial. Values update live
 *   via [onDragSetTicks] and are rounded once via [onDragRoundTicks] on
 *   release.
 * - **Two fingers** starting on the Fire (green) segment pinch the shooting
 *   duration: the change in the fingers' angular span, in seconds at the
 *   scale when the pinch began, is added to the duration and reported
 *   rounded via [onPinchSetShootingDuration]. A second finger landing during
 *   a tick drag converts the gesture into a pinch.
 *
 * All gestures are ignored unless [enabled].
 */
@Composable
internal fun DialGestureOverlay(
    size: Dp,
    ticks: List<Float>,
    ticksMax: Int,
    range: IntRange,
    fireStart: Float,
    fireDuration: Float,
    gapAngleDegrees: Float,
    ringThickness: Dp,
    enabled: Boolean,
    onDragSetTicks: (List<Float>) -> Unit,
    onDragRoundTicks: () -> Unit,
    onPinchSetShootingDuration: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentTicks by rememberUpdatedState(ticks)
    val currentTicksMax by rememberUpdatedState(ticksMax)
    val currentRange by rememberUpdatedState(range)
    val currentFireDuration by rememberUpdatedState(fireDuration)
    val currentEnabled by rememberUpdatedState(enabled)

    Box(
        modifier = modifier
            .size(size)
            .testTag(DIAL_GESTURE_TAG)
            .pointerInput(Unit) {
                val ringThicknessPx = ringThickness.toPx()
                val touchSlopPx = TickTouchSlop.toPx()

                // Hand-rolled instead of detectDragGestures/detectTransformGestures:
                // those detectors only report positions after touch slop is spent,
                // by which point the finger can already be too far from the tick
                // it started on for the hit test to find it — and the tick drag
                // and the pinch must share one gesture so a second finger can
                // take over a started tick drag.
                awaitEachGesture {
                    val down = awaitFirstDown()
                    if (!currentEnabled) return@awaitEachGesture

                    val canvasSize = this.size.width.toFloat()
                    val center = Offset(canvasSize / 2f, this.size.height / 2f)
                    // The dial rescales while the duration changes, so all
                    // angle→seconds mapping during this gesture uses the scale
                    // captured when it began.
                    val totalAtStart = currentTicksMax.toFloat()
                    val durationAtStart = currentFireDuration

                    fun tickValueAt(position: Offset): Float = dialAngleToTickValue(
                        angleFromCenterDegrees(center, position), totalAtStart, gapAngleDegrees
                    )

                    fun onRingNearFire(position: Offset): Boolean {
                        val distance = (position - center).getDistance()
                        return isWithinRingBand(distance, canvasSize, ringThicknessPx) &&
                            isWithinWedge(
                                tickValueAt(position),
                                fireStart,
                                fireStart + durationAtStart,
                                PinchWedgeSlackSeconds
                            )
                    }

                    var tickIndex: Int? = run {
                        val distance = (down.position - center).getDistance()
                        if (!isWithinRingBand(distance, canvasSize, ringThicknessPx)) return@run null
                        val tolerance = tickDragToleranceSeconds(
                            touchSlopPx, canvasSize / 2f, totalAtStart, gapAngleDegrees
                        )
                        nearestTickIndex(tickValueAt(down.position), currentTicks, tolerance)
                    }
                    var tickDragged = false
                    var pinchStartSpan: Float? = null
                    var lastSentDuration: Float? = null

                    while (true) {
                        val event = awaitPointerEvent()
                        val pressed = event.changes.filter { it.pressed }
                        if (pressed.isEmpty()) break

                        fun spanSeconds(changes: List<PointerInputChange>): Float =
                            abs(tickValueAt(changes[0].position) - tickValueAt(changes[1].position))

                        if (pinchStartSpan == null && pressed.size >= 2) {
                            if (pressed.take(2).all { onRingNearFire(it.position) }) {
                                pinchStartSpan = spanSeconds(pressed)
                                tickIndex = null
                            }
                        }

                        val startSpan = pinchStartSpan
                        if (startSpan != null) {
                            // One finger lifted mid-pinch: end the gesture rather
                            // than letting a later re-press jump the duration.
                            if (pressed.size < 2) break
                            val newDuration = (durationAtStart + (spanSeconds(pressed) - startSpan))
                                .roundToInt()
                                .toFloat()
                                .coerceIn(SHOOT_TIME_MIN.toFloat(), SHOOT_TIME_MAX.toFloat())
                            if (newDuration != lastSentDuration) {
                                lastSentDuration = newDuration
                                onPinchSetShootingDuration(newDuration)
                            }
                            pressed.forEach { it.consume() }
                        } else {
                            val index = tickIndex ?: continue
                            val change = pressed.firstOrNull { it.id == down.id } ?: break
                            if (change.positionChanged()) {
                                change.consume()
                                tickDragged = true
                                val newValue = tickValueAt(change.position)
                                    .coerceIn(currentRange.first.toFloat(), currentRange.last.toFloat())
                                onDragSetTicks(
                                    currentTicks.toMutableList().apply { this[index] = newValue }
                                )
                            }
                        }
                    }
                    if (tickDragged) onDragRoundTicks()
                }
            }
    )
}

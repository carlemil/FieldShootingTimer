package se.kjellstrand.fieldshootingtimer.ui

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import se.kjellstrand.fieldshootingtimer.resources.Res
import se.kjellstrand.fieldshootingtimer.resources.shooting_duration
import kotlin.math.abs
import kotlin.math.roundToInt

// Invisible gesture surface covering the dial; tagged for UI tests.
internal const val DIAL_GESTURE_TAG = "DialGestureSurface"

// Allowed shooting (Fire) duration in seconds, adjusted by pinching the
// Fire segment.
internal const val SHOOT_TIME_MIN = 1
internal const val SHOOT_TIME_MAX = 300

/** How much arc a finger may miss a tick flag by and still grab it. */
private val TickTouchSlop = 36.dp

/** How far a finger may miss the dial hand and still grab it for a scrub. */
private val HandTouchSlop = 24.dp

/**
 * How far the gesture surface extends beyond the dial on every side. The
 * flags sit outside the dial's edge, so a square surface the dial's exact
 * size only caught fingers near the diagonals (where the square's corners
 * reach past the circle) — flags at 0°/90°/180°/270° were untouchable just
 * outside the edge.
 */
private val GestureMargin = TickTouchSlop

/** How far outside the Fire wedge (in seconds) a pinch finger may start. */
private const val PinchWedgeSlackSeconds = 2f

/**
 * Transparent overlay for touch gestures on the dial ring:
 *
 * - **One finger** starting on the ring near a user tick grabs the nearest
 *   tick and follows the finger's angle around the dial. Values update live
 *   via [onDragSetTicks] and are rounded once via [onDragRoundTicks] on
 *   release.
 * - **One finger** landing on the dial hand itself (and not on a tick)
 *   scrubs the timer: the hand follows the finger's angle, reporting each
 *   position via [onScrub] and snapping to a whole second on release.
 * - **Two fingers** starting on the Fire (green) segment pinch the shooting
 *   duration: the change in the fingers' angular span, in seconds at the
 *   scale when the pinch began, is added to the duration and reported
 *   rounded via [onPinchSetShootingDuration]. A second finger landing during
 *   a tick drag converts the gesture into a pinch.
 *
 * Tick drags and pinches are ignored unless [editEnabled] (timer untouched);
 * the hand scrub is ignored unless [scrubEnabled] (timer not running).
 */
@Composable
internal fun DialGestureOverlay(
    size: Dp,
    ticks: List<Float>,
    ticksMax: Int,
    range: IntRange,
    fireStart: Float,
    fireDuration: Float,
    currentTime: Float,
    gapAngleDegrees: Float,
    ringThickness: Dp,
    editEnabled: Boolean,
    scrubEnabled: Boolean,
    onDragSetTicks: (List<Float>) -> Unit,
    onDragRoundTicks: () -> Unit,
    onPinchSetShootingDuration: (Float) -> Unit,
    onScrub: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentTicks by rememberUpdatedState(ticks)
    val currentTicksMax by rememberUpdatedState(ticksMax)
    val currentRange by rememberUpdatedState(range)
    val currentFireDuration by rememberUpdatedState(fireDuration)
    val currentEditEnabled by rememberUpdatedState(editEnabled)
    val currentScrubEnabled by rememberUpdatedState(scrubEnabled)
    val currentTimeState by rememberUpdatedState(currentTime)
    val shootingDurationLabel = stringResource(Res.string.shooting_duration)

    Box(
        // requiredSize so the surface can exceed the dial-sized parent box;
        // it stays centered on the dial.
        modifier = modifier
            .requiredSize(size + GestureMargin * 2)
            .testTag(DIAL_GESTURE_TAG)
            // Screen-reader path for the pinch gesture: the shooting duration
            // exposed as an adjustable progress node.
            .semantics {
                contentDescription = shootingDurationLabel
                progressBarRangeInfo = ProgressBarRangeInfo(
                    current = fireDuration,
                    range = SHOOT_TIME_MIN.toFloat()..SHOOT_TIME_MAX.toFloat()
                )
                if (editEnabled) {
                    setProgress { value ->
                        onPinchSetShootingDuration(
                            value.roundToInt().toFloat()
                                .coerceIn(SHOOT_TIME_MIN.toFloat(), SHOOT_TIME_MAX.toFloat())
                        )
                        true
                    }
                }
            }
            .pointerInput(Unit) {
                val dialSizePx = size.toPx()
                val ringThicknessPx = ringThickness.toPx()
                val touchSlopPx = TickTouchSlop.toPx()
                val handSlopPx = HandTouchSlop.toPx()

                // Hand-rolled instead of detectDragGestures/detectTransformGestures:
                // those detectors only report positions after touch slop is spent,
                // by which point the finger can already be too far from the tick
                // it started on for the hit test to find it — and the tick drag
                // and the pinch must share one gesture so a second finger can
                // take over a started tick drag.
                awaitEachGesture {
                    val down = awaitFirstDown()
                    if (!currentEditEnabled && !currentScrubEnabled) return@awaitEachGesture

                    // The surface is GestureMargin larger than the dial on
                    // every side; the dial stays centered in it, so all
                    // radius math uses dialSizePx, not the surface size.
                    val center = Offset(this.size.width / 2f, this.size.height / 2f)
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
                        return isWithinRingBand(distance, dialSizePx, ringThicknessPx) &&
                            isWithinWedge(
                                tickValueAt(position),
                                fireStart,
                                fireStart + durationAtStart,
                                PinchWedgeSlackSeconds
                            )
                    }

                    var tickIndex: Int? = if (!currentEditEnabled) null else run {
                        val distance = (down.position - center).getDistance()
                        if (!isWithinRingBand(distance, dialSizePx, ringThicknessPx)) return@run null
                        val tolerance = tickDragToleranceSeconds(
                            touchSlopPx, dialSizePx / 2f, totalAtStart, gapAngleDegrees
                        )
                        nearestTickIndex(tickValueAt(down.position), currentTicks, tolerance)
                    }
                    // A finger that grabbed no tick can grab the hand itself
                    // and scrub the paused timer.
                    val handGrabbed = tickIndex == null && currentScrubEnabled &&
                        distanceToHandPx(
                            center = center,
                            position = down.position,
                            handAngleDeg = DialGeometry.tickAngle(
                                currentTimeState.coerceIn(0f, totalAtStart),
                                totalAtStart,
                                gapAngleDegrees
                            ),
                            handLengthPx = dialSizePx / 2f
                        ) <= handSlopPx
                    var lastScrub: Float? = null
                    var tickDragged = false
                    var pinchStartSpan: Float? = null
                    var lastSentDuration: Float? = null

                    while (true) {
                        val event = awaitPointerEvent()
                        val pressed = event.changes.filter { it.pressed }
                        if (pressed.isEmpty()) break

                        fun spanSeconds(changes: List<PointerInputChange>): Float =
                            abs(tickValueAt(changes[0].position) - tickValueAt(changes[1].position))

                        if (pinchStartSpan == null && pressed.size >= 2 &&
                            currentEditEnabled && !handGrabbed
                        ) {
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
                        } else if (handGrabbed) {
                            val change = pressed.firstOrNull { it.id == down.id } ?: break
                            if (change.positionChanged()) {
                                change.consume()
                                val newTime = tickValueAt(change.position)
                                lastScrub = newTime
                                onScrub(newTime)
                            }
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
                    // Snap the scrubbed hand to a whole second once released.
                    lastScrub?.let { onScrub(it.roundToInt().toFloat()) }
                }
            }
    )
}

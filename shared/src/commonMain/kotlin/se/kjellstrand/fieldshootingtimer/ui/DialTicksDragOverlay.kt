package se.kjellstrand.fieldshootingtimer.ui

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Invisible drag surface covering the dial; tagged for UI tests.
internal const val DIAL_TICKS_TAG = "DialTicksDragSurface"

/** How much arc a finger may miss a tick block by and still grab it. */
private val TickTouchSlop = 24.dp

/**
 * Transparent overlay that lets the user drag the tick blocks on the dial
 * ring. A drag starting on the ring near a tick grabs the nearest one and
 * follows the finger's angle around the dial; values update live via
 * [onDragSetTicks] and are rounded once via [onDragRoundTicks] on release —
 * the same contract as [MultiThumbSlider]'s thumbs.
 */
@Composable
internal fun DialTicksDragOverlay(
    size: Dp,
    ticks: List<Float>,
    ticksMax: Int,
    range: IntRange,
    gapAngleDegrees: Float,
    ringThickness: Dp,
    enabled: Boolean,
    onDragSetTicks: (List<Float>) -> Unit,
    onDragRoundTicks: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentTicks by rememberUpdatedState(ticks)
    val currentTicksMax by rememberUpdatedState(ticksMax)
    val currentRange by rememberUpdatedState(range)
    val currentEnabled by rememberUpdatedState(enabled)

    Box(
        modifier = modifier
            .size(size)
            .testTag(DIAL_TICKS_TAG)
            .pointerInput(Unit) {
                val ringThicknessPx = ringThickness.toPx()
                val touchSlopPx = TickTouchSlop.toPx()

                // Hand-rolled instead of detectDragGestures: that detector only
                // reports the drag's start position after touch slop is spent,
                // by which point the finger can already be too far from the
                // tick it started on for the hit test to find it.
                awaitEachGesture {
                    val down = awaitFirstDown()
                    val canvasSize = this.size.width.toFloat()
                    val center = Offset(canvasSize / 2f, this.size.height / 2f)
                    val distance = (down.position - center).getDistance()

                    if (!currentEnabled ||
                        !isWithinRingBand(distance, canvasSize, ringThicknessPx)
                    ) return@awaitEachGesture

                    val touchValue = dialAngleToTickValue(
                        angleFromCenterDegrees(center, down.position),
                        currentTicksMax.toFloat(),
                        gapAngleDegrees
                    )
                    val tolerance = tickDragToleranceSeconds(
                        touchSlopPx, canvasSize / 2f, currentTicksMax.toFloat(), gapAngleDegrees
                    )
                    val index = nearestTickIndex(touchValue, currentTicks, tolerance)
                        ?: return@awaitEachGesture

                    var dragged = false
                    drag(down.id) { change ->
                        change.consume()
                        dragged = true
                        val newValue = dialAngleToTickValue(
                            angleFromCenterDegrees(center, change.position),
                            currentTicksMax.toFloat(),
                            gapAngleDegrees
                        ).coerceIn(currentRange.first.toFloat(), currentRange.last.toFloat())
                        onDragSetTicks(
                            currentTicks.toMutableList().apply { this[index] = newValue }
                        )
                    }
                    if (dragged) onDragRoundTicks()
                }
            }
    )
}

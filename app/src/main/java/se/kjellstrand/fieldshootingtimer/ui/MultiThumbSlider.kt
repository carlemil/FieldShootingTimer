package se.kjellstrand.fieldshootingtimer.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import se.kjellstrand.fieldshootingtimer.ui.theme.Paddings
import kotlin.math.roundToInt

@Composable
fun MultiThumbSlider(
    thumbValues: List<Float>,
    onHorizontalDragSetThumbValues: State<(List<Float>) -> Unit>,
    onHorizontalDragRoundThumbValues: State<() -> Unit>,
    range: IntRange,
    trackColor: Color = Color.Gray,
    thumbColor: Color = Color.Blue,
    trackHeight: Dp = Paddings.Small,
    thumbHeight: Dp = 18.dp,
    thumbWidth: Dp = 4.dp,
    trackGapWidth: Dp = 3.dp,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val density = LocalDensity.current
    val thumbHeightPx = with(density) { thumbHeight.toPx() }
    val thumbWidthPx = with(density) { thumbWidth.toPx() }
    val trackHeightPx = with(density) { trackHeight.toPx() }
    val trackGapWidthPx = with(density) { trackGapWidth.toPx() }
    val currentThumbValues by rememberUpdatedState(thumbValues)
    val currentRange by rememberUpdatedState(range)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(thumbHeight * 2)
            .padding(horizontal = Paddings.Small)
    ) {
        val maxWidth = constraints.maxWidth
        val segmentWidth = maxWidth / ((currentRange.last + 1) - currentRange.first)
        val firstAndLastSegmentWidth = segmentWidth / 2f
        val trackWidth = maxWidth - segmentWidth
        val integerMarkers = currentRange.first..currentRange.last + 1
        Canvas(modifier = Modifier.fillMaxSize()) {
            val lineSegments = mutableListOf<Pair<Float, Float>>()
            var previousEnd = 0f

            integerMarkers.forEach { marker ->
                var markerOffset = ((marker - currentRange.first).toFloat() /
                        (currentRange.last - currentRange.first)) * trackWidth + firstAndLastSegmentWidth
                if (markerOffset < 0) markerOffset = 0f

                val isThumbAtMarker = currentThumbValues.any { thumb ->
                    thumb.roundToInt() == marker
                }

                val cutoutWidth =
                    if (isThumbAtMarker) trackGapWidthPx * 2 else trackGapWidthPx

                var newEnd = (markerOffset - cutoutWidth)
                if (newEnd > maxWidth) newEnd = maxWidth.toFloat()
                lineSegments.add(previousEnd to newEnd)
                previousEnd = markerOffset + cutoutWidth
            }

            lineSegments.forEach { (start, end) ->
                drawLine(
                    color = trackColor,
                    start = Offset(start, size.height / 2),
                    end = Offset(end, size.height / 2),
                    strokeWidth = trackHeightPx,
                    cap = StrokeCap.Butt
                )
            }

            currentThumbValues.map { value ->
                toThumbOffset(value, currentRange, trackWidth, firstAndLastSegmentWidth)
            }.forEach { thumbOffset ->
                drawLine(
                    color = if (enabled) thumbColor else trackColor,
                    start = Offset(thumbOffset, (size.height / 2) - thumbHeightPx),
                    end = Offset(thumbOffset, (size.height / 2) + thumbHeightPx),
                    strokeWidth = thumbWidthPx,
                    cap = StrokeCap.Round
                )
            }
        }

        if (enabled) {
            currentThumbValues.forEachIndexed { index, value ->
                Box(modifier = Modifier
                    .offset {
                        IntOffset(
                            x = toThumbOffset(
                                value,
                                currentRange,
                                trackWidth,
                                firstAndLastSegmentWidth
                            ).roundToInt(),
                            y = 0
                        )
                    }
                    .size(thumbHeight * 2)
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(onHorizontalDrag = { change, _ ->
                            change.consume()
                            val currentThumbOffset =
                                ((currentThumbValues[index] - currentRange.first) / (currentRange.last - currentRange.first)) * maxWidth.toFloat()
                            val newOffset = (currentThumbOffset + change.position.x).coerceIn(
                                0f, maxWidth.toFloat()
                            )
                            val newValue =
                                (newOffset / maxWidth) * (currentRange.last - currentRange.first) + currentRange.first

                            val updatedValues =
                                currentThumbValues
                                    .toMutableList()
                                    .apply { this[index] = newValue }
                            onHorizontalDragSetThumbValues.value(updatedValues)
                        }, onDragEnd = {
                            onHorizontalDragRoundThumbValues.value()
                        })
                    })
            }
        }
    }
}

private fun toThumbOffset(
    thumbValue: Float,
    range: IntRange,
    sliderWidth: Int,
    firstAndLastSegmentWidth: Float
): Float {
    return ((thumbValue - range.first) / (range.last - range.first)) * sliderWidth + firstAndLastSegmentWidth
}

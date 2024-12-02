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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import se.kjellstrand.fieldshootingtimer.ui.theme.Paddings
import kotlin.math.roundToInt

@Composable
fun MultiThumbSlider(
    thumbValues: List<Float>,
    onHorizontalDragSetThumbValues: State<(List<Float>) -> Unit>,
    onHorizontalDragRoundThumbValues: State<() -> Unit> = rememberUpdatedState({}),
    range: IntRange,
    trackColor: Color = Color.Gray,
    thumbColor: Color = Color.Blue,
    inactiveColor: Color = Color.LightGray,
    trackHeight: Dp = 12.dp,
    thumbHeight: Dp = 18.dp,
    thumbWidth: Dp = 8.dp,
    trackGapWidth: Dp = 0.8.dp,
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
        val maxHeight = constraints.maxHeight
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
                    if (isThumbAtMarker) thumbWidthPx+trackGapWidthPx*2 else trackGapWidthPx

                var newEnd = (markerOffset - cutoutWidth/2)
                if (newEnd > maxWidth) newEnd = maxWidth.toFloat()
                lineSegments.add(previousEnd to newEnd)
                previousEnd = markerOffset + cutoutWidth/2
            }

            if (lineSegments.isNotEmpty()) {
                drawRoundedCap(90f,
                    if (enabled) trackColor else inactiveColor,
                    lineSegments.first().first,
                    trackHeightPx)
            }

            lineSegments.forEach { (start, end) ->
                drawLine(
                    color = if (enabled) trackColor else inactiveColor,
                    start = Offset(start, size.height / 2),
                    end = Offset(end, size.height / 2),
                    strokeWidth = trackHeightPx,
                    cap = StrokeCap.Butt
                )
            }

            if (lineSegments.isNotEmpty()) {
                drawRoundedCap(270f,
                    if (enabled) trackColor else inactiveColor,
                    lineSegments.last().second,
                    trackHeightPx)
            }

            currentThumbValues.map { value ->
                toThumbOffset(value, currentRange, trackWidth, firstAndLastSegmentWidth)
            }.forEach { thumbOffset ->
                drawLine(
                    color = if (enabled) thumbColor else inactiveColor,
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
                            x = (toThumbOffset(
                                value,
                                currentRange,
                                trackWidth,
                                firstAndLastSegmentWidth
                            ) - thumbWidthPx * 1.5).roundToInt(),
                            y = (maxHeight / 2 - thumbHeightPx).toInt()
                        )
                    }
                    .size(width = thumbWidth * 3, height = thumbHeight * 2)
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

private fun DrawScope.drawRoundedCap(
    startAngle: Float,
    color: Color,
    x: Float,
    trackHeightPx: Float
) {
    drawArc(
        color = color,
        startAngle = startAngle,
        sweepAngle = 180f,
        useCenter = true,
        topLeft = Offset(
            x = x - trackHeightPx / 2,
            y = (size.height / 2) - trackHeightPx / 2
        ),
        size = Size(
            width = trackHeightPx,
            height = trackHeightPx
        )
    )
}

private fun toThumbOffset(
    thumbValue: Float,
    range: IntRange,
    sliderWidth: Int,
    firstAndLastSegmentWidth: Float
): Float {
    return ((thumbValue - range.first) / (range.last - range.first)) * sliderWidth + firstAndLastSegmentWidth
}

@Preview(showBackground = true, widthDp = 400, heightDp = 100)
@Composable
fun MultiThumbSliderPreview() {
    val thumbValues = listOf(5f, 6f, 7f, 9f, 12f)
    val range = 3..27
    val onHorizontalDragSetThumbValues = rememberUpdatedState { _: List<Float> -> }
    val onHorizontalDragRoundThumbValues = rememberUpdatedState { }

    MultiThumbSlider(
        thumbValues = thumbValues,
        onHorizontalDragSetThumbValues = onHorizontalDragSetThumbValues,
        onHorizontalDragRoundThumbValues = onHorizontalDragRoundThumbValues,
        range = range,
        modifier = Modifier.padding(16.dp)
    )
}

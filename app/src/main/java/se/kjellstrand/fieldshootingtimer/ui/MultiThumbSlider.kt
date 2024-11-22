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
import androidx.compose.ui.unit.dp
import se.kjellstrand.fieldshootingtimer.ui.theme.Paddings

@Composable
fun MultiThumbSlider(
    thumbValues: List<Float>,
    onHorizontalDragSetThumbValues: State<(List<Float>) -> Unit>,
    onHorizontalDragRoundThumbValues: State<() -> Unit>,
    range: IntRange,
    trackColor: Color = Color.Gray,
    thumbColor: Color = Color.Blue,
    trackHeight: Dp = Paddings.Small,
    thumbRadius: Dp = 12.dp,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val thumbRadiusPx = with(density) { thumbRadius.toPx() }
    val trackHeightPx = with(density) { trackHeight.toPx() }
    val currentThumbValuesState by rememberUpdatedState(thumbValues)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(thumbRadius * 2)
            .padding(horizontal = Paddings.Small)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val thumbOffsets = toThumbOffsets(currentThumbValuesState, range, constraints.maxWidth)

            drawLine(
                color = trackColor,
                start = Offset(0f, size.height / 2),
                end = Offset(constraints.maxWidth.toFloat(), size.height / 2),
                strokeWidth = trackHeightPx,
                cap = StrokeCap.Round
            )

            thumbOffsets.forEach { thumbOffset ->
                drawLine(
                    color = thumbColor,
                    start = Offset(thumbOffset, (size.height / 2) - thumbRadiusPx),
                    end = Offset(thumbOffset, (size.height / 2) + thumbRadiusPx),
                    strokeWidth = trackHeightPx / 2f,
                    cap = StrokeCap.Round
                )
            }
        }

        currentThumbValuesState.forEachIndexed { index, _ ->
            Box(modifier = Modifier
                .offset(x = with(density) {
                    val currentThumbOffset =
                        ((currentThumbValuesState[index] - range.first) / (range.last - range.first)) * constraints.maxWidth
                    currentThumbOffset.toDp()
                } - thumbRadius)
                .size(thumbRadius * 2)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(onHorizontalDrag = { change, _ ->
                        change.consume()
                        val currentThumbOffset =
                            ((currentThumbValuesState[index] - range.first) / (range.last - range.first)) * constraints.maxWidth.toFloat()
                        val newOffset = (currentThumbOffset + change.position.x).coerceIn(
                            0f, constraints.maxWidth.toFloat()
                        )
                        val newValue =
                            (newOffset / constraints.maxWidth) * (range.last - range.first) + range.first

                        val updatedValues =
                            currentThumbValuesState
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

private fun toThumbOffsets(
    thumbValues: List<Float>, range: IntRange, sliderWidth: Int
) = thumbValues.map { value ->
    ((value - range.first) / (range.last - range.first)) * sliderWidth
}

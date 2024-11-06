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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


@Composable
fun MultiThumbSlider(
    timerViewModel: TimerViewModel,
    modifier: Modifier = Modifier,
    range: ClosedFloatingPointRange<Float> = 0f..100f,
    trackColor: Color = Color.Gray,
    activeTrackColor: Color = Color.Blue,
    thumbColor: Color = Color.Blue,
    trackHeight: Dp = 8.dp,
    thumbRadius: Dp = 12.dp
) {
    val timerUiState by timerViewModel.uiState.collectAsState()

    val density = LocalDensity.current
    val thumbRadiusPx = with(density) { thumbRadius.toPx() }
    val trackHeightPx = with(density) { trackHeight.toPx() }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(thumbRadius * 2)
            .padding(horizontal = thumbRadius)
    ) {
        val sliderWidth = constraints.maxWidth.toFloat()

        val thumbOffsets = timerUiState.thumbValues.map { value ->
            ((value - range.start) / (range.endInclusive - range.start)) * sliderWidth
        }
        Canvas(modifier = Modifier.fillMaxSize()) {
            println("Draw the slider with values ${timerUiState.thumbValues}")

            drawLine(
                color = trackColor,
                start = Offset(0f, size.height / 2),
                end = Offset(sliderWidth, size.height / 2),
                strokeWidth = trackHeightPx,
                cap = StrokeCap.Round
            )

            thumbOffsets.zipWithNext { start, end ->
                drawLine(
                    color = activeTrackColor,
                    start = Offset(start, size.height / 2),
                    end = Offset(end, size.height / 2),
                    strokeWidth = trackHeightPx,
                    cap = StrokeCap.Round
                )
            }

            thumbOffsets.forEach { thumbOffset ->
                drawCircle(
                    color = thumbColor,
                    radius = thumbRadiusPx,
                    center = Offset(thumbOffset, size.height / 2)
                )
            }
        }

        timerUiState.thumbValues.forEachIndexed { index, _ ->
            Box(
                modifier = Modifier
                    .offset(x = with(density) {
                        val currentThumbOffset =
                            ((timerUiState.thumbValues[index] - range.start) / (range.endInclusive - range.start)) * sliderWidth
                        currentThumbOffset.toDp()
                    } - thumbRadius)
                    .size(thumbRadius * 2)
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onHorizontalDrag = { change, dragAmount ->
                                change.consume()

                                val currentThumbOffset =
                                    ((timerUiState.thumbValues[index] - range.start) / (range.endInclusive - range.start)) * sliderWidth
                                val newOffset =
                                    (currentThumbOffset + dragAmount).coerceIn(0f, sliderWidth)
                                val newValue =
                                    (newOffset / sliderWidth) * (range.endInclusive - range.start) + range.start

                                val updatedValues = timerUiState.thumbValues
                                    .toMutableList()
                                    .apply { this[index] = newValue }

                                timerViewModel.setThumbValues(updatedValues)
                            }
                        )
                    }
            )
        }
    }
}
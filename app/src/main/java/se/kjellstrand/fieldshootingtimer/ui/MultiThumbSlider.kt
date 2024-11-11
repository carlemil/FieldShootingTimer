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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun MultiThumbSlider(
    timerViewModel: TimerViewModel,
    range: IntRange,
    trackColor: Color = Color.Gray,
    thumbColor: Color = Color.Blue,
    trackHeight: Dp = 8.dp,
    thumbRadius: Dp = 12.dp,
    modifier: Modifier = Modifier
) {
    val timerUiState by timerViewModel.uiState.collectAsState()

    val density = LocalDensity.current
    val thumbRadiusPx = with(density) { thumbRadius.toPx() }
    val trackHeightPx = with(density) { trackHeight.toPx() }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(thumbRadius * 2)
            .padding(horizontal = 8.dp)
    ) {
        val sliderWidth = constraints.maxWidth.toFloat()

        val thumbOffsets = remember(timerUiState.thumbValues, range) {
            toThumbOffsets(timerUiState, range, sliderWidth)
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

            thumbOffsets.forEach { thumbOffset ->
                drawLine(
                    color = thumbColor,
                    start = Offset(thumbOffset, (size.height / 2) - thumbRadiusPx),
                    end = Offset(thumbOffset, (size.height / 2) + thumbRadiusPx),
                    strokeWidth = trackHeightPx/2f,
                    cap = StrokeCap.Round
                )
            }
        }

        timerUiState.thumbValues.forEachIndexed { index, _ ->
            Box(modifier = Modifier
                .offset(x = with(density) {
                    val currentThumbOffset =
                        ((timerUiState.thumbValues[index] - range.first) / (range.last - range.first)) * sliderWidth
                    currentThumbOffset.toDp()
                } - thumbRadius)
                .size(thumbRadius * 2)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(onHorizontalDrag = { change, _ ->
                        change.consume()
                        val currentThumbOffset =
                            ((timerUiState.thumbValues[index] - range.first) / (range.last - range.first)) * sliderWidth
                        val newOffset = (currentThumbOffset + change.position.x).coerceIn(
                            0f, sliderWidth
                        )
                        val newValue =
                            (newOffset / sliderWidth) * (range.last - range.first) + range.first

                        val updatedValues = timerUiState.thumbValues
                            .toMutableList()
                            .apply { this[index] = newValue }

                        timerViewModel.setThumbValues(updatedValues)
                    }, onDragEnd = {
                        timerViewModel.setThumbValues(timerUiState.thumbValues
                            .toMutableList()
                            .map { value ->
                                value
                                    .roundToInt()
                                    .toFloat()
                            })
                    })
                })
        }
    }
}

private fun toThumbOffsets(
    timerUiState: TimerUiState, range: IntRange, sliderWidth: Float
) = timerUiState.thumbValues.map { value ->
    ((value - range.first) / (range.last - range.first)) * sliderWidth
}
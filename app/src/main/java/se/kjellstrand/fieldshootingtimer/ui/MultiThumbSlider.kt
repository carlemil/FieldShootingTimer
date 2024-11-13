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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
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
    val thumbValues by timerViewModel.thumbValuesFlow.collectAsState(initial = listOf())

    val onHorizontalDragSetThumbValues = rememberUpdatedState { updatedValues: List<Float> ->
        timerViewModel.setThumbValues(updatedValues)
    }

    val onDragEndSetThumbValues = rememberUpdatedState { updatedValues: List<Float> ->
        timerViewModel.setThumbValues(updatedValues.map { it.roundToInt().toFloat() })
    }

    val density = LocalDensity.current
    val thumbRadiusPx = with(density) { thumbRadius.toPx() }
    val trackHeightPx = with(density) { trackHeight.toPx() }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(thumbRadius * 2)
            .padding(horizontal = 8.dp)
    ) {
        val sliderWidth by remember(constraints.maxWidth) {
            derivedStateOf { constraints.maxWidth }
        }

        val thumbOffsets by remember(thumbValues, range, sliderWidth) {
            derivedStateOf { toThumbOffsets(thumbValues, range, sliderWidth) }
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            drawLine(
                color = trackColor,
                start = Offset(0f, size.height / 2),
                end = Offset(sliderWidth.toFloat(), size.height / 2),
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

        thumbValues.forEachIndexed { index, _ ->
            Box(modifier = Modifier
                .offset(x = with(density) {
                    val currentThumbOffset =
                        ((thumbValues[index] - range.first) / (range.last - range.first)) * sliderWidth
                    currentThumbOffset.toDp()
                } - thumbRadius)
                .size(thumbRadius * 2)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(onHorizontalDrag = { change, _ ->
                        change.consume()
                        val currentThumbOffset =
                            ((thumbValues[index] - range.first) / (range.last - range.first)) * sliderWidth
                        val newOffset = (currentThumbOffset + change.position.x).coerceIn(
                            0f, sliderWidth.toFloat()
                        )
                        val newValue =
                            (newOffset / sliderWidth) * (range.last - range.first) + range.first

                        val updatedValues =
                            thumbValues
                                .toMutableList()
                                .apply { this[index] = newValue }

                        onHorizontalDragSetThumbValues.value(updatedValues)
                    }, onDragEnd = {
                        onDragEndSetThumbValues.value(thumbValues)
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

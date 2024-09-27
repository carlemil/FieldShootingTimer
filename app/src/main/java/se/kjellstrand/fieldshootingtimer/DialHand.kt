// File: DialHand.kt
package se.kjellstrand.fieldshootingtimer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun DialHand(
    currentTime: Float,
    totalTime: Float,
    availableAngle: Float,
    gapAngleDegrees: Float = 30f,
    size: Dp,
    ringThickness: Dp,
    borderWidth: Dp = 2.dp,
    handColor: Color = Color.White,
    borderColor: Color = Color.Black,
    handThickness: Dp = 4.dp,
    overshootPercent: Float = 0.1f
) {
    Canvas(modifier = Modifier.size(size)) {
        val sizePx = size.toPx()
        val ringThicknessPx = ringThickness.toPx()
        val borderWidthPx = borderWidth.toPx()
        val handThicknessPx = handThickness.toPx()

        val center = Offset(sizePx / 2, sizePx / 2)
        val radius = (sizePx - ringThicknessPx) / 2

        val startAngle = 270f + (gapAngleDegrees / 2)

        val angleDegrees = startAngle + (currentTime / totalTime) * availableAngle - 180f
        val angleRadians = Math.toRadians(angleDegrees.toDouble()).toFloat()

        val handLength = radius + (radius * overshootPercent)

        val endX = center.x + handLength * cos(angleRadians)
        val endY = center.y + handLength * sin(angleRadians)
        val end = Offset(endX, endY)

        drawLine(
            color = borderColor,
            start = center,
            end = end,
            strokeWidth = handThicknessPx + borderWidthPx * 2,
            cap = StrokeCap.Round
        )

        drawLine(
            color = handColor,
            start = center,
            end = end,
            strokeWidth = handThicknessPx,
            cap = StrokeCap.Round
        )

        drawCircle(
            color = borderColor,
            radius = handThicknessPx + borderWidthPx,
            center = center
        )
        drawCircle(
            color = handColor,
            radius = handThicknessPx,
            center = center
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DialHandPreview() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(500.dp)
            .background(Color.LightGray)
    ) {
        DialHand(
            currentTime = 50f,
            totalTime = 100f,
            availableAngle = 270f,
            size = 200.dp,
            ringThickness = 20.dp,
            borderWidth = 4.dp,
            handColor = Color.White,
            borderColor = Color.Black,
            handThickness = 8.dp,
            overshootPercent = 0.1f
        )
    }
}
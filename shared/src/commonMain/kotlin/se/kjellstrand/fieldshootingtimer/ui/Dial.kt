package se.kjellstrand.fieldshootingtimer.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun Dial(
    modifier: Modifier = Modifier,
    segmentColors: List<Color>,
    sweepAngles: List<Float>,
    gapAngleDegrees: Float = 30f,
    ringThickness: Dp = 20.dp,
    borderColor: Color = Color.Black,
    borderWidth: Dp = 2.dp,
    size: Dp = 200.dp
) {
    val availableAngle = DialGeometry.availableAngle(gapAngleDegrees)

    Canvas(
        modifier = Modifier
            .size(size)
            .then(modifier)
    ) {
        val canvasSize = size.toPx()

        val ringThicknessPx = ringThickness.toPx()
        val borderWidthPx = borderWidth.toPx()
        val totalPadding = (ringThicknessPx / 2) + (borderWidthPx / 2)

        val rect = Rect(
            left = totalPadding,
            top = totalPadding,
            right = canvasSize - totalPadding,
            bottom = canvasSize - totalPadding
        )

        var startAngle = DialGeometry.startAngle(gapAngleDegrees)

        sweepAngles.forEachIndexed { index, sweepAngle ->
            val color = segmentColors.getOrNull(index) ?: Color.Gray
            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = rect.topLeft,
                size = rect.size,
                style = Stroke(width = ringThicknessPx, cap = StrokeCap.Butt)
            )
            startAngle += sweepAngle
        }

        val outerBorderRect = Rect(
            left = rect.left - (ringThicknessPx / 2) - (borderWidthPx / 2),
            top = rect.top - (ringThicknessPx / 2) - (borderWidthPx / 2),
            right = rect.right + (ringThicknessPx / 2) + (borderWidthPx / 2),
            bottom = rect.bottom + (ringThicknessPx / 2) + (borderWidthPx / 2)
        )
        val innerBorderRect = Rect(
            left = rect.left + (ringThicknessPx / 2) + (borderWidthPx / 2),
            top = rect.top + (ringThicknessPx / 2) + (borderWidthPx / 2),
            right = rect.right - (ringThicknessPx / 2) - (borderWidthPx / 2),
            bottom = rect.bottom - (ringThicknessPx / 2) - (borderWidthPx / 2)
        )

        val borderPath = Path().apply {
            arcTo(
                rect = outerBorderRect,
                startAngleDegrees = DialGeometry.startAngle(gapAngleDegrees),
                sweepAngleDegrees = availableAngle,
                forceMoveTo = true
            )
            arcTo(
                rect = innerBorderRect,
                startAngleDegrees = DialGeometry.startAngle(gapAngleDegrees) + availableAngle,
                sweepAngleDegrees = -availableAngle,
                forceMoveTo = false
            )
            close()
        }

        drawPath(
            path = borderPath,
            color = borderColor,
            style = Stroke(width = borderWidthPx, cap = StrokeCap.Butt)
        )
    }
}

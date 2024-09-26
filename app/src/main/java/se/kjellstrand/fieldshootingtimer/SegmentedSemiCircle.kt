package se.kjellstrand.fieldshootingtimer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import se.kjellstrand.fieldshootingtimer.ui.theme.FieldShootingTimerTheme

@Composable
fun SegmentedSemiCircle(
    modifier: Modifier = Modifier,
    semiCircleColors: SemiCircleColors,
    sweepAngles: List<Float>,
    gapAngleDegrees: Float = 30f,
    ringThickness: Dp = 20.dp,
    borderColor: Color = Color.Black,
    borderWidth: Dp = 2.dp,
    size: Dp = 200.dp
) {
    val availableAngle = 360f - gapAngleDegrees

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

        var startAngle = 270f - (availableAngle / 2)

        sweepAngles.forEachIndexed { index, sweepAngle ->
            val color = semiCircleColors.segmentColors.getOrNull(index) ?: Color.Gray
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
                startAngleDegrees = 270f - (availableAngle / 2),
                sweepAngleDegrees = availableAngle,
                forceMoveTo = true
            )
            arcTo(
                rect = innerBorderRect,
                startAngleDegrees = 270f - (availableAngle / 2) + availableAngle,
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

@Preview(showBackground = true)
@Composable
fun SegmentedSemiCirclePreview() {
    FieldShootingTimerTheme {
        val semiCircleColors = SemiCircleColors(
            segmentColors = listOf(
                MaterialTheme.colorScheme.primary,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                Color.Red,Color.Blue,Color.Green
            )
        )
        val sweepAngles = listOf(6f, 5f, 40f, 9F, 1F,8F)

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            SegmentedSemiCircle(
                semiCircleColors = semiCircleColors,
                sweepAngles = sweepAngles,
                gapAngleDegrees = 30f,
                ringThickness = 20.dp,
                borderColor = Color.Black,
                borderWidth = 2.dp,
                size = 200.dp
            )
        }
    }
}
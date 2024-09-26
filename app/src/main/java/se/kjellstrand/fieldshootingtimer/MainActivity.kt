package se.kjellstrand.fieldshootingtimer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import se.kjellstrand.fieldshootingtimer.ui.theme.FieldShootingTimerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FieldShootingTimerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CenteredSemiCircle()
                }
            }
        }
    }
}

@Composable
fun CenteredSemiCircle() {
    // Define the segment colors from the theme
    val semiCircleColors = SemiCircleColors(
        segmentColors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
            MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
            Color.Red,Color.Blue,Color.Green
        )
    )
    val sweepAngles = listOf(60f, 50f, 40f, 90F, 10F,80F)

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

@Preview(showBackground = true)
@Composable
fun CenteredSemiCirclePreview() {
    FieldShootingTimerTheme {
        CenteredSemiCircle()
    }
}
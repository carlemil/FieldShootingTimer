package se.kjellstrand.fieldshootingtimer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.isActive
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
                    Timer()
                }
            }
        }
    }
}

@Composable
fun Timer() {
    FieldShootingTimerTheme {
        val timeInSecondsForEachSegment = listOf(3f, 10f, 8f, 3f)
        val totalTime = timeInSecondsForEachSegment.sum()

        var isRunning by remember { mutableStateOf(false) }
        var currentTime by remember { mutableFloatStateOf(0f) }

        LaunchedEffect(isRunning) {
            if (isRunning) {
                val startTimeMillis = withFrameMillis { it }
                var lastFrameTimeMillis = startTimeMillis
                while (currentTime < totalTime && isActive) {
                    val frameTimeMillis = withFrameMillis { it }
                    val deltaTime = (frameTimeMillis - lastFrameTimeMillis) / 1000f
                    currentTime += deltaTime
                    if (currentTime >= totalTime) {
                        currentTime = totalTime
                        isRunning = false
                        break
                    }
                    lastFrameTimeMillis = frameTimeMillis
                }
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.weight(10f)
            ) {
                Timer(
                    currentTime = currentTime,
                    dialColors = DialColors(
                        colors = listOf(
                            Color.LightGray,
                            Color.Yellow,
                            Color.Green,
                            Color.Red
                        )
                    ),
                    gapAngleDegrees = 30f,
                    timesForSegments = timeInSecondsForEachSegment,
                    ringThickness = 30.dp,
                    borderColor = Color.Black,
                    borderWidth = 2.dp,
                    size = 300.dp,
                    badgeRadius = 15.dp,
                    handColor = Color.White,
                    handThickness = 4.dp
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(onClick = { isRunning = !isRunning }) {
                if (isRunning) {
                    Icon(imageVector = Icons.Default.Call, contentDescription = "Pause")
                } else {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Play")
                }
            }

            Spacer(modifier = Modifier.weight(3f))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CenteredSemiCirclePreview() {
    FieldShootingTimerTheme {
        Timer()
    }
}
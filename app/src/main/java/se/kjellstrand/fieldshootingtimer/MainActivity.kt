package se.kjellstrand.fieldshootingtimer

import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
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
                    MainScreen()
                }
            }
        }
    }
}

@Composable
fun MainScreen() {
    FieldShootingTimerTheme {
        // Example: total 25 seconds shooting time
        val timeInSecondsForEachSegment = listOf(7f, 3f, 8f, 3f, 3f, 1f)
        val totalTime = timeInSecondsForEachSegment.sum()
        val timerSize = 300.dp

        var isRunning by remember { mutableStateOf(false) }
        var currentTime by remember { mutableFloatStateOf(0f) }
        var isFinished by remember { mutableStateOf(false) }
        val playedAudioIndices = remember { mutableSetOf<Int>() } // Moved outside LaunchedEffect

        val context = LocalContext.current

        val segmentStartTimes = remember {
            val startTimesList = mutableListOf<Float>()
            var cumulativeTime = 0f
            for (time in timeInSecondsForEachSegment) {
                startTimesList.add(cumulativeTime)
                cumulativeTime += time
            }
            startTimesList
        }

        val audioCues = listOf(
            AudioCue(time = segmentStartTimes[0], resId = R.raw.beep_1),
            AudioCue(time = segmentStartTimes[1], resId = R.raw.beep_1),
            AudioCue(time = segmentStartTimes[2], resId = R.raw.beep_1),
            AudioCue(time = segmentStartTimes[3], resId = R.raw.beep_1),
            AudioCue(time = segmentStartTimes[4], resId = R.raw.beep_1),
            AudioCue(time = segmentStartTimes[5], resId = R.raw.beep_1)
        )

        LaunchedEffect(isRunning) {
            if (isRunning) {
                for ((index, audioCue) in audioCues.withIndex()) {
                    if (currentTime >= audioCue.time && !playedAudioIndices.contains(index)) {
                        val mediaPlayer = MediaPlayer.create(context, audioCue.resId)
                        mediaPlayer.start()
                        mediaPlayer.setOnCompletionListener {
                            mediaPlayer.release()
                        }
                        playedAudioIndices.add(index)
                    }
                }

                val startTimeMillis = withFrameMillis { it }
                var lastFrameTimeMillis = startTimeMillis

                while (currentTime < totalTime && isActive) {
                    val frameTimeMillis = withFrameMillis { it }
                    val deltaTime = (frameTimeMillis - lastFrameTimeMillis) / 1000f
                    currentTime += deltaTime

                    for ((index, audioCue) in audioCues.withIndex()) {
                        if (currentTime >= audioCue.time && !playedAudioIndices.contains(index)) {
                            val mediaPlayer = MediaPlayer.create(context, audioCue.resId)
                            mediaPlayer.start()
                            mediaPlayer.setOnCompletionListener {
                                mediaPlayer.release()
                            }
                            playedAudioIndices.add(index)
                        }
                    }

                    if (currentTime >= totalTime) {
                        currentTime = totalTime
                        isRunning = false
                        isFinished = true
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
                            Color.LightGray,
                            Color.Green,
                            Color.Green,
                            Color.Yellow,
                            Color.Red
                        )
                    ),
                    gapAngleDegrees = 30f,
                    timesForSegments = timeInSecondsForEachSegment,
                    ringThickness = 30.dp,
                    borderColor = Color.Black,
                    borderWidth = 2.dp,
                    size = timerSize,
                    badgeRadius = 15.dp,
                    handColor = Color.White,
                    handThickness = 4.dp
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            ControlButton(
                isRunning = isRunning,
                isFinished = isFinished,
                buttonSize = timerSize/2,
                onPlayPauseResetClicked = {
                    when {
                        isFinished -> {
                            // Reset the timer
                            currentTime = 0f
                            isFinished = false
                        }

                        else -> {
                            isRunning = !isRunning
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.weight(3f))
        }
    }
}

@Composable
fun ControlButton(
    isRunning: Boolean,
    isFinished: Boolean,
    onPlayPauseResetClicked: () -> Unit,
    buttonSize: Dp = 56.dp
) {
    Button(
        onClick = { onPlayPauseResetClicked() },
        modifier = Modifier.size(buttonSize),
        shape = CircleShape,
        contentPadding = PaddingValues(0.dp),  // Remove default padding
    ) {
        when {
            isFinished -> {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.skip_previous),
                    contentDescription = "Reset",
                    modifier = Modifier.size(buttonSize * 0.6f)
                )
            }
            isRunning -> {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.pause),
                    contentDescription = "Pause",
                    modifier = Modifier.size(buttonSize * 0.6f)
                )
            }
            else -> {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.play_arrow),
                    contentDescription = "Play",
                    modifier = Modifier.size(buttonSize * 0.6f)
                )
            }
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
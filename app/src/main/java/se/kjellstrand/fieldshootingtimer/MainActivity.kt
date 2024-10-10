package se.kjellstrand.fieldshootingtimer

import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import kotlin.math.roundToInt

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
    var isRunning by remember { mutableStateOf(false) }
    var currentTime by remember { mutableFloatStateOf(0f) }
    var isFinished by remember { mutableStateOf(false) }
    val playedAudioIndices = remember { mutableSetOf<Int>() }

    val context = LocalContext.current

    var sliderValue by remember { mutableFloatStateOf(8f) }

    val timeInSecondsForEachSegment = listOf(7f, 3f, sliderValue.roundToInt().toFloat(), 3f, 1f)
    val totalTime = timeInSecondsForEachSegment.sum()
    val timerSize = 300.dp

    val segmentStartTimes = remember {
        val startTimesList = mutableListOf<Float>()
        var cumulativeTime = 0f
        for (time in timeInSecondsForEachSegment) {
            startTimesList.add(cumulativeTime)
            cumulativeTime += time
        }
        startTimesList
    }

    val audioCues = remember(timeInSecondsForEachSegment) {
        List(timeInSecondsForEachSegment.size) { index ->
            AudioCue(time = segmentStartTimes[index], resId = R.raw.beep_1)
        }
    }

    LaunchedEffect(isRunning, timeInSecondsForEachSegment) {
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
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Gray)
    ) {

        Spacer(modifier = Modifier.weight(2f))

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
                        Color.Yellow,
                        Color.Red
                    )
                ),
                gapAngleDegrees = 30f,
                timesForSegments = timeInSecondsForEachSegment,
                ringThickness = 40.dp,
                borderColor = Color.Black,
                borderWidth = 2.dp,
                size = timerSize,
                badgeRadius = 15.dp,
                handColor = Color.White,
                handThickness = 6.dp
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        ControlButton(
            isRunning = isRunning,
            isFinished = isFinished,
            buttonSize = timerSize / 2f,
            onPlayPauseResetClicked = {
                when {
                    isFinished -> {
                        currentTime = 0f
                        isFinished = false
                    }

                    else -> {
                        isRunning = !isRunning
                    }
                }
            }
        )

        Spacer(modifier = Modifier.weight(1f))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Adjust Time: ${sliderValue.toInt()} sec")
            Slider(
                value = sliderValue,
                onValueChange = { newValue ->
                    sliderValue = newValue
                    currentTime = 0f
                    isRunning = false
                    isFinished = false
                    playedAudioIndices.clear()
                },
                colors = SliderDefaults.colors(
                    thumbColor = Color.Green,
                    activeTrackColor = Color.Green,
                    inactiveTrackColor = Color.Green.copy(alpha = 0.3f),
                    activeTickColor = Color.Yellow,
                    inactiveTickColor = Color.Green.copy(alpha = 0.3f)
                ),
                valueRange = 1f..30f,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        Spacer(modifier = Modifier.weight(3f))
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
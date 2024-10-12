package se.kjellstrand.fieldshootingtimer

import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.isActive
import se.kjellstrand.fieldshootingtimer.ui.theme.BackgroundColor
import se.kjellstrand.fieldshootingtimer.ui.theme.CeaseFireSegmentColor
import se.kjellstrand.fieldshootingtimer.ui.theme.FieldShootingTimerTheme
import se.kjellstrand.fieldshootingtimer.ui.theme.HandBackgroundColor
import se.kjellstrand.fieldshootingtimer.ui.theme.PlugWeaponSegmentColor
import se.kjellstrand.fieldshootingtimer.ui.theme.PrepareSegmentColor
import se.kjellstrand.fieldshootingtimer.ui.theme.ShootSegmentColor
import se.kjellstrand.fieldshootingtimer.ui.theme.SliderActiveTrackColor
import se.kjellstrand.fieldshootingtimer.ui.theme.SliderInactiveTrackColor
import se.kjellstrand.fieldshootingtimer.ui.theme.SliderThumbColor
import se.kjellstrand.fieldshootingtimer.ui.theme.TimerBordersColor

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
    val playedAudioIndices = remember(isRunning) { mutableSetOf<Int>() }
    var sliderValue by remember { mutableFloatStateOf(5f) }
    val ceaseFireTime = 3f
    val timeInSecondsForEachSegment =
        listOf(7f, 3f, sliderValue.toInt().toFloat(), ceaseFireTime, 1f)
    val totalTime = timeInSecondsForEachSegment.sum()

    val context = LocalContext.current
    val timerSize = 300.dp

    val segmentStartTimes = remember(timeInSecondsForEachSegment) {
        val startTimesList = mutableListOf<Float>()
        var cumulativeTime = 0f
        for (time in timeInSecondsForEachSegment) {
            startTimesList.add(cumulativeTime)
            cumulativeTime += time
        }
        startTimesList
    }
    val audioCues = remember(timeInSecondsForEachSegment) {
        listOf(
            AudioCue(time = segmentStartTimes[0], resId = R.raw.tio_sekunder_kvar_cut),
            AudioCue(time = segmentStartTimes[1], resId = R.raw.fardiga_cut),
            AudioCue(time = segmentStartTimes[2], resId = R.raw.eld_cut),
            AudioCue(time = segmentStartTimes[3], resId = R.raw.eld_upp_hor_cut),
            AudioCue(time = segmentStartTimes[4], resId = R.raw.patron_ur_proppa_vapen_cut)
        )
    }

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
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
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
                        PrepareSegmentColor,
                        PrepareSegmentColor,
                        ShootSegmentColor,
                        CeaseFireSegmentColor,
                        PlugWeaponSegmentColor
                    )
                ),
                gapAngleDegrees = 30f,
                timesForSegments = timeInSecondsForEachSegment,
                ringThickness = 60.dp,
                borderColor = TimerBordersColor,
                borderWidth = 2.dp,
                size = timerSize,
                badgeRadius = 15.dp,
                handColor = HandBackgroundColor,
                handThickness = 8.dp
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        ControlButton(
            isRunning = isRunning,
            isFinished = isFinished,
            buttonSize = timerSize / 2f,
            onPlayStopResetClicked = {
                when {
                    isFinished -> {
                        currentTime = 0f
                        isFinished = false
                    }

                    !isRunning -> {
                        isRunning = true
                    }

                    else -> {
                        isRunning = false
                        currentTime = 0f
                        isFinished = false
                    }
                }
            }
        )

        Spacer(modifier = Modifier.weight(1f))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(
                    R.string.shooting_time,
                    sliderValue.toInt(),
                    ceaseFireTime.toInt(),
                    (sliderValue + ceaseFireTime).toInt()
                ),
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )
            Slider(
                value = sliderValue,
                enabled = !isRunning,
                onValueChange = { newValue ->
                    sliderValue = newValue
                    currentTime = 0f
                    isRunning = false
                    isFinished = false
                    playedAudioIndices.clear()
                },
                colors = SliderDefaults.colors(
                    thumbColor = SliderThumbColor,
                    activeTrackColor = SliderActiveTrackColor,
                    inactiveTrackColor = SliderInactiveTrackColor
                ),
                valueRange = 1f..27f,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        Spacer(modifier = Modifier.weight(3f))
    }
}

@Preview(showBackground = true)
@Composable
fun CenteredSemiCirclePreview() {
    FieldShootingTimerTheme {
        Timer()
    }
}
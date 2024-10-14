package se.kjellstrand.fieldshootingtimer

import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.isActive
import se.kjellstrand.fieldshootingtimer.ui.theme.*

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
                    MainScreen(TimerViewModel())
                }
            }
        }
    }
}

@Composable
fun MainScreen(
    timerViewModel: TimerViewModel
) {
    val timerUiState by timerViewModel.uiState.collectAsState()

    val playedAudioIndices = remember(timerUiState.timerRunningState) { mutableSetOf<Int>() }
    var shootingDuration by remember { mutableFloatStateOf(timerUiState.shootingDuration) }
    val ceaseFireDuration = 3f
    val segmentDurations =
        listOf(7f, 3f, shootingDuration.toInt().toFloat(), ceaseFireDuration, 1f)
    val totalDuration = segmentDurations.sum()

    val context = LocalContext.current
    val timerSize = 300.dp

    val audioCueStartTimes = remember(segmentDurations) {
        val startTimes = mutableListOf<Float>()
        var cumulativeTime = 0f
        for (time in segmentDurations) {
            startTimes.add(cumulativeTime)
            cumulativeTime += time
        }
        startTimes
    }
    val audioCues = remember(segmentDurations) {
        listOf(
            AudioCue(time = audioCueStartTimes[0], resId = R.raw.tio_sekunder_kvar_cut),
            AudioCue(time = audioCueStartTimes[1], resId = R.raw.fardiga_cut),
            AudioCue(time = audioCueStartTimes[2], resId = R.raw.eld_cut),
            AudioCue(time = audioCueStartTimes[3], resId = R.raw.eld_upp_hor_cut),
            AudioCue(time = audioCueStartTimes[4], resId = R.raw.patron_ur_proppa_vapen_cut)
        )
    }

    LaunchedEffect(timerUiState.timerRunningState) {
        if (timerUiState.timerRunningState == TimerState.Running) {
            for ((index, audioCue) in audioCues.withIndex()) {
                if (timerUiState.currentTime >= audioCue.time && !playedAudioIndices.contains(index)) {
                    playAudioCue(context, audioCue)
                    playedAudioIndices.add(index)
                }
            }

            val startTimeMillis = withFrameMillis { it }
            var lastFrameTimeMillis = startTimeMillis
            while (timerUiState.currentTime < totalDuration && isActive) {
                val frameTimeMillis = withFrameMillis { it }
                val deltaTime = (frameTimeMillis - lastFrameTimeMillis) / 1000f
                timerViewModel.setCurrentTime(timerUiState.currentTime + deltaTime)
                for ((index, audioCue) in audioCues.withIndex()) {
                    if (timerUiState.currentTime >= audioCue.time &&
                        !playedAudioIndices.contains(index)
                    ) {
                        playAudioCue(context, audioCue)
                        playedAudioIndices.add(index)
                    }
                }

                if (timerUiState.currentTime >= totalDuration) {
                    timerViewModel.setCurrentTime(totalDuration)
                    timerViewModel.setTimerState(TimerState.Finished)
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
                currentTime = timerUiState.currentTime,
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
                timesForSegments = segmentDurations,
                ringThickness = 60.dp,
                borderColor = TimerBordersColor,
                borderWidth = 2.dp,
                size = timerSize,
                badgeRadius = 15.dp,
                handColor = HandBackgroundColor,
                handThickness = 8.dp,
                badgesVisible = timerUiState.badgesVisible
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        PlayButton(timerUiState, timerSize, timerViewModel)

        Spacer(modifier = Modifier.weight(1f))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(
                    R.string.shooting_time,
                    shootingDuration.toInt(),
                    ceaseFireDuration.toInt(),
                    (shootingDuration + ceaseFireDuration).toInt()
                ),
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )
            Slider(
                value = shootingDuration,
                enabled = timerUiState.timerRunningState == TimerState.NotStarted,
                onValueChange = { newValue ->
                    shootingDuration = newValue
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

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Checkbox(
                checked = timerUiState.badgesVisible,
                onCheckedChange = { checked ->
                    timerViewModel.setBadgesVisible(checked)
                }
            )
            Text(
                text = stringResource(R.string.show_segment_times),
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.weight(3f))
    }
}

@Composable
private fun PlayButton(
    timerUiState: TimerUiState,
    timerSize: Dp,
    timerViewModel: TimerViewModel
) {
    TimerStateButton(
        timerUiState = timerUiState,
        buttonSize = timerSize / 2f,
        onPlayStopResetClicked = {
            when (timerUiState.timerRunningState) {
                TimerState.NotStarted -> {
                    timerViewModel.setTimerState(TimerState.Running)
                }

                TimerState.Running -> {
                    timerViewModel.setTimerState(TimerState.Stopped)
                }

                TimerState.Stopped, TimerState.Finished -> {
                    timerViewModel.setCurrentTime(0f)
                    timerViewModel.setTimerState(TimerState.NotStarted)
                }
            }
        }
    )
}

private fun playAudioCue(
    context: Context,
    audioCue: AudioCue
) {
    val mediaPlayer = MediaPlayer.create(context, audioCue.resId)
    mediaPlayer.start()
    mediaPlayer.setOnCompletionListener {
        mediaPlayer.release()
    }
}

@Preview(showBackground = true)
@Composable
fun CenteredSemiCirclePreview() {
    FieldShootingTimerTheme {
        Timer()
    }
}
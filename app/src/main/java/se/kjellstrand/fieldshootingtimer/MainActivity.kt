package se.kjellstrand.fieldshootingtimer

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.isActive
import se.kjellstrand.fieldshootingtimer.audio.AudioCue
import se.kjellstrand.fieldshootingtimer.audio.AudioCueType
import se.kjellstrand.fieldshootingtimer.audio.AudioManager
import se.kjellstrand.fieldshootingtimer.ui.DialColors
import se.kjellstrand.fieldshootingtimer.ui.Timer
import se.kjellstrand.fieldshootingtimer.ui.TimerState
import se.kjellstrand.fieldshootingtimer.ui.TimerStateButton
import se.kjellstrand.fieldshootingtimer.ui.TimerUiState
import se.kjellstrand.fieldshootingtimer.ui.TimerViewModel
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

private const val TAG = "ShootingTimer"

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
    val ceaseFireDuration = 3f
    val segmentDurations =
        listOf(7f, 3f, timerUiState.shootingDuration.toInt().toFloat(), ceaseFireDuration, 1f)
    val totalDuration = segmentDurations.sum()

    val context = LocalContext.current

    val audioManager = remember { AudioManager(context) }

    val audioCueStartTimes = remember(segmentDurations) {
        val startTimes = mutableListOf<Float>()
        var cumulativeTime = 0f
        for (time in segmentDurations) {
            startTimes.add(cumulativeTime)
            cumulativeTime += time
        }
        startTimes
    }
    val audioCues = remember(audioCueStartTimes) {
        listOf(
            AudioCue(time = audioCueStartTimes[0], cueType = AudioCueType.TenSecondsLeft),
            AudioCue(time = audioCueStartTimes[1], cueType = AudioCueType.Ready),
            AudioCue(time = audioCueStartTimes[2], cueType = AudioCueType.Fire),
            AudioCue(time = audioCueStartTimes[3], cueType = AudioCueType.CeaseFire),
            AudioCue(time = audioCueStartTimes[4], cueType = AudioCueType.UnloadWeapon)
        )
    }

    LaunchedEffect(timerUiState.timerRunningState) {
        if (timerUiState.timerRunningState == TimerState.Running) {
            playAudioCue(audioCues, timerUiState, playedAudioIndices, audioManager)

            val startTimeMillis = withFrameMillis { it }
            var lastFrameTimeMillis = startTimeMillis
            while (timerUiState.currentTime < totalDuration && isActive) {
                val frameTimeMillis = withFrameMillis { it }
                val deltaTime = (frameTimeMillis - lastFrameTimeMillis) / 1000f
                timerViewModel.setCurrentTime(timerUiState.currentTime + deltaTime)
                playAudioCue(audioCues, timerUiState, playedAudioIndices, audioManager)

                if (timerUiState.currentTime >= totalDuration) {
                    timerViewModel.setCurrentTime(totalDuration)
                    timerViewModel.setTimerState(TimerState.Finished)
                    break
                }
                lastFrameTimeMillis = frameTimeMillis
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            audioManager.release()
        }
    }

    val configuration = LocalConfiguration.current

    when (configuration.orientation) {
        Configuration.ORIENTATION_PORTRAIT -> {
            PortraitUI(
                timerViewModel,
                segmentDurations,
                playedAudioIndices,
                300.dp,
                ceaseFireDuration
            )
        }

        else -> {
            LandscapeUI(
                timerViewModel,
                segmentDurations,
                playedAudioIndices,
                250.dp,
                ceaseFireDuration
            )
        }
    }
}

@Composable
fun LandscapeUI(
    timerViewModel: TimerViewModel,
    segmentDurations: List<Float>,
    playedAudioIndices: MutableSet<Int>,
    timerSize: Dp = 300.dp,
    ceaseFireDuration: Float
) {
    val timerUiState by timerViewModel.uiState.collectAsState()
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .systemBarsPadding()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(16.dp)
        ) {
            ShootTimer(timerUiState, segmentDurations, timerSize)
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(16.dp)
                .navigationBarsPadding()
        ) {
            Spacer(modifier = Modifier.weight(1f))
            PlayButton(timerViewModel, timerSize)
            Spacer(modifier = Modifier.height(16.dp))
            ShootTimeSlider(
                timerViewModel,
                ceaseFireDuration,
                playedAudioIndices
            )
            Spacer(modifier = Modifier.height(16.dp))
            ShowSegmentTimes(timerViewModel)
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun PortraitUI(
    timerViewModel: TimerViewModel,
    segmentDurations: List<Float>,
    playedAudioIndices: MutableSet<Int>,
    timerSize: Dp,
    ceaseFireDuration: Float
) {
    val timerUiState by timerViewModel.uiState.collectAsState()
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
    ) {
        Spacer(modifier = Modifier.weight(2f))
        ShootTimer(timerUiState, segmentDurations, timerSize)
        Spacer(modifier = Modifier.weight(1f))
        PlayButton(timerViewModel, timerSize)
        Spacer(modifier = Modifier.weight(1f))
        ShootTimeSlider(
            timerViewModel,
            ceaseFireDuration,
            playedAudioIndices
        )
        ShowSegmentTimes(timerViewModel)
        Spacer(modifier = Modifier.weight(3f))
    }
}

@Composable
private fun ShootTimer(
    timerUiState: TimerUiState,
    segmentDurations: List<Float>,
    timerSize: Dp
) {
    Box(
        contentAlignment = Alignment.Center
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
}

@Composable
private fun ShootTimeSlider(
    timerViewModel: TimerViewModel,
    ceaseFireDuration: Float,
    playedAudioIndices: MutableSet<Int>
) {
    val timerUiState by timerViewModel.uiState.collectAsState()
    val shootingDuration = timerUiState.shootingDuration
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
            onValueChange = { newShootingDuration ->
                timerViewModel.setShootingTime(newShootingDuration)
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
}

@Composable
private fun ShowSegmentTimes(
    timerViewModel: TimerViewModel
) {
    val timerUiState by timerViewModel.uiState.collectAsState()
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
}

@Composable
private fun PlayButton(
    timerViewModel: TimerViewModel,
    timerSize: Dp
) {
    val timerUiState by timerViewModel.uiState.collectAsState()
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
    audioCues: List<AudioCue>,
    timerUiState: TimerUiState,
    playedAudioIndices: MutableSet<Int>,
    audioManager: AudioManager
) {
    for ((index, audioCue) in audioCues.withIndex()) {
        if (timerUiState.currentTime >= audioCue.time &&
            !playedAudioIndices.contains(index)
        ) {
            audioManager.playSound(audioCue.cueType)
            playedAudioIndices.add(index)
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
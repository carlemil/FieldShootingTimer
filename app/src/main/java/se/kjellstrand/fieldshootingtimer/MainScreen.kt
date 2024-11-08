package se.kjellstrand.fieldshootingtimer

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
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
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.isActive
import se.kjellstrand.fieldshootingtimer.audio.AudioCue
import se.kjellstrand.fieldshootingtimer.audio.AudioCueType
import se.kjellstrand.fieldshootingtimer.audio.AudioManager
import se.kjellstrand.fieldshootingtimer.ui.MultiThumbSlider
import se.kjellstrand.fieldshootingtimer.ui.PlayButton
import se.kjellstrand.fieldshootingtimer.ui.ShootTimeSlider
import se.kjellstrand.fieldshootingtimer.ui.ShootTimer
import se.kjellstrand.fieldshootingtimer.ui.ShowSegmentTimes
import se.kjellstrand.fieldshootingtimer.ui.TimerState
import se.kjellstrand.fieldshootingtimer.ui.TimerViewModel
import se.kjellstrand.fieldshootingtimer.ui.theme.BackgroundColor
import se.kjellstrand.fieldshootingtimer.ui.theme.FieldShootingTimerTheme
import kotlin.math.roundToInt

const val TEN_SECONDS_LEFT_DURATION = 7f
const val READY_DURATION = 3f
const val CEASE_FIRE_DURATION = 3f
const val SILENCE_DURATION = 2f
const val UNLOAD_WEAPON_DURATION = 3f
const val VISITATION_DURATION = 2f

class MainScreen : ComponentActivity() {

    private val timerViewModel: TimerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FieldShootingTimerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(timerViewModel = timerViewModel)
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
    val segmentDurations = remember(timerUiState.shootingDuration) {
        listOf(
            TEN_SECONDS_LEFT_DURATION,
            READY_DURATION,
            timerUiState.shootingDuration.toInt().toFloat(),
            CEASE_FIRE_DURATION,
            SILENCE_DURATION,
            UNLOAD_WEAPON_DURATION,
            VISITATION_DURATION
        )
    }

    val context = LocalContext.current

    val audioManager = remember { AudioManager(context) }
    val audioCues = remember(timerUiState.timerRunningState) {
        val cues = mutableListOf<AudioCue>()
        var time = 0f

        cues.add(AudioCue(time, AudioCueType.TenSecondsLeft))
        time += TEN_SECONDS_LEFT_DURATION

        cues.add(AudioCue(time, AudioCueType.Ready))
        time += READY_DURATION

        cues.add(AudioCue(time, AudioCueType.Fire))
        time += timerUiState.shootingDuration.toInt().toFloat()

        cues.add(AudioCue(time, AudioCueType.CeaseFire))
        time += CEASE_FIRE_DURATION + SILENCE_DURATION

        cues.add(AudioCue(time, AudioCueType.UnloadWeapon))
        time += UNLOAD_WEAPON_DURATION

        cues.add(AudioCue(time, AudioCueType.Visitation))
        cues
    }

    val totalDuration = remember(segmentDurations) { segmentDurations.sum() }

    val rangeOffset = TEN_SECONDS_LEFT_DURATION + READY_DURATION
    val range = remember(timerUiState.shootingDuration) {
        val range = IntRange(
            (rangeOffset + 1).toInt(),
            (timerUiState.shootingDuration + rangeOffset + CEASE_FIRE_DURATION - 1).toInt()
        )
        range
    }

    LaunchedEffect(timerUiState.timerRunningState) {
        if (timerUiState.timerRunningState == TimerState.Running) {
            audioManager.playAudioCue(audioCues, timerUiState, playedAudioIndices)

            val startTimeMillis = withFrameMillis { it }
            var lastFrameTimeMillis = startTimeMillis
            while (timerUiState.currentTime < totalDuration && isActive) {
                val frameTimeMillis = withFrameMillis { it }
                val deltaTime = (frameTimeMillis - lastFrameTimeMillis) / 1000f
                timerViewModel.setCurrentTime(timerUiState.currentTime + deltaTime)
                audioManager.playAudioCue(audioCues, timerUiState, playedAudioIndices)

                if (timerUiState.currentTime >= totalDuration) {
                    timerViewModel.setCurrentTime(totalDuration)
                    timerViewModel.setTimerState(TimerState.Finished)
                    break
                }
                lastFrameTimeMillis = frameTimeMillis
            }
        }
    }

    LaunchedEffect(timerUiState.shootingDuration) {
        timerViewModel.setThumbValues(timerUiState.thumbValues.filter { it.roundToInt() in range })
    }

    DisposableEffect(Unit) {
        onDispose {
            audioManager.release()
        }
    }

    when (LocalConfiguration.current.orientation) {
        Configuration.ORIENTATION_PORTRAIT -> {
            PortraitUI(
                timerViewModel,
                segmentDurations,
                range,
                playedAudioIndices,
                300.dp
            )
        }

        else -> {
            LandscapeUI(
                timerViewModel,
                segmentDurations,
                playedAudioIndices,
                280.dp
            )
        }
    }
}

@Composable
fun LandscapeUI(
    timerViewModel: TimerViewModel,
    segmentDurations: List<Float>,
    playedAudioIndices: MutableSet<Int>,
    timerSize: Dp
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
                .weight(9f)
                .fillMaxHeight()
                .padding(16.dp)
                .navigationBarsPadding()
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                ShootTimer(timerUiState, segmentDurations, timerSize)
                PlayButton(timerViewModel, timerSize)
            }

        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .weight(9f)
                .fillMaxHeight()
                .padding(16.dp)
                .navigationBarsPadding()
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(16.dp))
            ShootTimeSlider(
                timerViewModel,
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
    range: IntRange,
    playedAudioIndices: MutableSet<Int>,
    timerSize: Dp
) {
    val timerUiState by timerViewModel.uiState.collectAsState()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
    ) {
        Spacer(modifier = Modifier.weight(2f))
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            ShootTimer(timerUiState, segmentDurations, timerSize)
            PlayButton(timerViewModel, timerSize)
        }

        Spacer(modifier = Modifier.weight(1f))

        ShootTimeSlider(
            timerViewModel,
            playedAudioIndices
        )
        Spacer(modifier = Modifier.weight(1f))

        ShowSegmentTimes(timerViewModel)

        Spacer(modifier = Modifier.weight(1f))

        MultiThumbSlider(timerViewModel, range)

        Button(onClick = {
            if (timerUiState.thumbValues.size < (range.last - range.first)) {
                timerViewModel.setThumbValues(
                    timerUiState.thumbValues + findNextFreeThumbSpot(
                        range,
                        timerUiState.thumbValues
                    )
                )
            }
        }) { Text("+") }
        Button(onClick = {
            timerViewModel.setThumbValues(timerUiState.thumbValues.dropLast(1))
        }) { Text("-") }

        Spacer(modifier = Modifier.weight(1f))

    }
}

fun findNextFreeThumbSpot(range: IntRange, takenSpots: List<Float>): Float {
    val center = (range.first + range.last) / 2
    val maxDistance = (range.last - range.first) / 2

    for (distance in 0..maxDistance) {
        val forward = center + distance
        val backward = center - distance

        if (forward in range && takenSpots.find { it.roundToInt() == forward } == null) {
            return forward.toFloat()
        }
        if (backward in range && takenSpots.find { it.roundToInt() == backward } == null) {
            return backward.toFloat()
        }
    }
    return center.toFloat()
}

@Preview(showBackground = true)
@Composable
fun PortraitUIPreview() {
    val tvm = TimerViewModel()
    tvm.setShootingTime(5f)
    tvm.setCurrentTime(0f)
    tvm.setTimerState(TimerState.NotStarted)

    val segmentDurations = listOf(
        TEN_SECONDS_LEFT_DURATION,
        READY_DURATION,
        5f,
        CEASE_FIRE_DURATION,
        SILENCE_DURATION,
        UNLOAD_WEAPON_DURATION,
        VISITATION_DURATION
    )
    val ticks = listOf(5, 6, 7, 8, 11, 12, 13, 14, 15, 16)
    PortraitUI(tvm, segmentDurations, IntRange(10, 20), mutableSetOf(), 300.dp)
}

@Preview(
    uiMode = Configuration.UI_MODE_TYPE_NORMAL,
    widthDp = 640,
    heightDp = 360,
    showBackground = true,
    name = "Landscape Preview"
)
@Composable
fun LandscapeUIPreview() {
    val tvm = TimerViewModel()
    tvm.setShootingTime(5f)
    tvm.setCurrentTime(0f)
    tvm.setTimerState(TimerState.NotStarted)

    val segmentDurations = listOf(
        TEN_SECONDS_LEFT_DURATION,
        READY_DURATION,
        5f,
        CEASE_FIRE_DURATION,
        SILENCE_DURATION,
        UNLOAD_WEAPON_DURATION,
        VISITATION_DURATION
    )
    val ticks = listOf(5, 6, 7, 8, 11, 12, 13, 14, 15, 16)
    LandscapeUI(tvm, segmentDurations, mutableSetOf(), 280.dp)
}
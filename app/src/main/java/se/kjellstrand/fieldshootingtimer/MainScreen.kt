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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import se.kjellstrand.fieldshootingtimer.audio.AudioCue
import se.kjellstrand.fieldshootingtimer.audio.AudioManager
import se.kjellstrand.fieldshootingtimer.ui.Command
import se.kjellstrand.fieldshootingtimer.ui.CommandList
import se.kjellstrand.fieldshootingtimer.ui.PlayButton
import se.kjellstrand.fieldshootingtimer.ui.ShootTimeSlider
import se.kjellstrand.fieldshootingtimer.ui.ShootTimer
import se.kjellstrand.fieldshootingtimer.ui.ShowSegmentTimes
import se.kjellstrand.fieldshootingtimer.ui.TicksAdjuster
import se.kjellstrand.fieldshootingtimer.ui.TimerState
import se.kjellstrand.fieldshootingtimer.ui.TimerViewModel
import se.kjellstrand.fieldshootingtimer.ui.theme.GrayColor
import se.kjellstrand.fieldshootingtimer.ui.theme.FieldShootingTimerTheme
import kotlin.math.roundToInt

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
        Command.entries.filter { it.duration >= 0 }.map {
            when (it) {
                Command.Fire -> timerUiState.shootingDuration
                else -> it.duration.toFloat()
            }
        }
    }

    val context = LocalContext.current

    val audioManager = remember { AudioManager(context) }
    val audioCues = remember(timerUiState.timerRunningState) {
        val cues = mutableListOf<AudioCue>()
        var time = 0f

        cues.add(AudioCue(time, Command.TenSecondsLeft))
        time += Command.TenSecondsLeft.duration

        cues.add(AudioCue(time, Command.Ready))
        time += Command.Ready.duration

        cues.add(AudioCue(time, Command.Fire))
        time += timerUiState.shootingDuration.toInt().toFloat()

        cues.add(AudioCue(time, Command.CeaseFire))
        time += Command.CeaseFire.duration

        cues.add(AudioCue(time, Command.UnloadWeapon))
        time += Command.UnloadWeapon.duration

        cues.add(AudioCue(time, Command.Visitation))
        cues
    }

    val totalDuration = remember(segmentDurations) { segmentDurations.sum() }

    val rangeOffset = Command.TenSecondsLeft.duration + Command.Ready.duration
    val range = remember(timerUiState.shootingDuration) {
        val range = IntRange(
            (rangeOffset + 1),
            (timerUiState.shootingDuration + rangeOffset + Command.CeaseFire.duration - 1).toInt()
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
                range,
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
    range: IntRange,
    playedAudioIndices: MutableSet<Int>,
    timerSize: Dp
) {
    val timerUiState by timerViewModel.uiState.collectAsState()
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(GrayColor)
            .systemBarsPadding()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxHeight()
                .padding(start = 16.dp)
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
                .fillMaxHeight()
                .padding(8.dp)
                .navigationBarsPadding()
        ) {
            Settings(timerViewModel, range, playedAudioIndices, segmentDurations)
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
            .background(GrayColor)
            .systemBarsPadding()
    ) {
        Spacer(modifier = Modifier.padding(16.dp))
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            ShootTimer(timerUiState, segmentDurations, timerSize)
            PlayButton(timerViewModel, timerSize)
        }
        Spacer(modifier = Modifier.padding(24.dp))
        Settings(timerViewModel, range, playedAudioIndices, segmentDurations)
    }
}

@Composable
fun Settings(
    timerViewModel: TimerViewModel,
    range: IntRange,
    playedAudioIndices: MutableSet<Int>,
    segmentDurations: List<Float>
) {
    val timerUiState by timerViewModel.uiState.collectAsState()
    val highlightedIndex = calculateHighlightedIndex(timerUiState.currentTime, segmentDurations)

    ShowSegmentTimes(timerViewModel)
    Spacer(modifier = Modifier.padding(8.dp))

    val shootingDuration by timerViewModel.shootingDurationFlow.collectAsState(
        initial = 5f, context = Dispatchers.Main
    )
    val timerRunningState by timerViewModel.timerRunningStateFlow.collectAsState(
        initial = TimerState.NotStarted, context = Dispatchers.Main
    )

    ShootTimeSlider(
        shootingDuration = shootingDuration,
        timerRunningState = timerRunningState,
        onValueChange = { duration ->
            timerViewModel.setShootingTime(duration.roundToInt().toFloat())
            playedAudioIndices.clear()
        }
    )
    Spacer(modifier = Modifier.padding(8.dp))
    TicksAdjuster(timerViewModel, range)
    Spacer(modifier = Modifier.padding(8.dp))
    CommandList(highlightedIndex)
}

fun calculateHighlightedIndex(currentTime: Float, highlightDurations: List<Float>): Int {
    var accumulatedTime = 0f
    highlightDurations.forEachIndexed { index, duration ->
        accumulatedTime += duration
        if (currentTime < accumulatedTime) {
            return index + 2 // +2 due to the initial commands not being part of the timer duration
        }
    }
    return 7 // Default to the last command if time exceeds all durations
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

    val segmentDurations = listOf(7f, 3f, 5f, 3f, 4f, 2f)
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

    val segmentDurations = listOf(7f, 3f, 5f, 3f, 4f, 2f)
    LandscapeUI(tvm, segmentDurations, IntRange(10, 20), mutableSetOf(), 280.dp)
}
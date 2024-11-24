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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import se.kjellstrand.fieldshootingtimer.ui.TimerRunningState
import se.kjellstrand.fieldshootingtimer.ui.TimerViewModel
import se.kjellstrand.fieldshootingtimer.ui.theme.FieldShootingTimerTheme
import se.kjellstrand.fieldshootingtimer.ui.theme.GrayColor
import se.kjellstrand.fieldshootingtimer.ui.theme.Paddings
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
    val currentTime by timerViewModel.currentTimeFlow.collectAsState(
        initial = 0f, context = Dispatchers.Main
    )
    val shootingDuration by timerViewModel.shootingDurationFlow.collectAsState(
        initial = 0f, context = Dispatchers.Main
    )
    val timerRunningState by timerViewModel.timerRunningStateFlow.collectAsState(
        initial = TimerRunningState.NotStarted, context = Dispatchers.Main
    )
    val thumbValues by timerViewModel.thumbValuesFlow.collectAsState(
        initial = listOf(), context = Dispatchers.Main
    )

    var playedAudioIndices by rememberSaveable(timerRunningState) { mutableStateOf(setOf<Int>()) }
    val clearPlayedAudioIndices: () -> Unit = {
        playedAudioIndices = emptySet()
    }
    val onAddPlayedAudioIndex: (Int) -> Unit = { index ->
        val mutableSet = playedAudioIndices.toMutableSet()
        mutableSet.add(index)
        playedAudioIndices = mutableSet.toSet()
    }

    val segmentDurations by rememberSaveable(shootingDuration) {
        mutableStateOf(Command.entries.filter { it.duration >= 0 }.map {
            when (it) {
                Command.Fire -> shootingDuration
                else -> it.duration.toFloat()
            }
        })
    }

    val context = LocalContext.current

    val audioManager = remember { AudioManager(context) }
    val audioCues by rememberSaveable(timerRunningState) {
        val cues = mutableListOf<AudioCue>()
        var time = 0f

        cues.add(AudioCue(time, Command.TenSecondsLeft))
        time += Command.TenSecondsLeft.duration

        cues.add(AudioCue(time, Command.Ready))
        time += Command.Ready.duration

        cues.add(AudioCue(time, Command.Fire))
        time += shootingDuration.toInt().toFloat()

        cues.add(AudioCue(time, Command.CeaseFire))
        time += Command.CeaseFire.duration

        cues.add(AudioCue(time, Command.UnloadWeapon))
        time += Command.UnloadWeapon.duration

        cues.add(AudioCue(time, Command.Visitation))
        mutableStateOf(cues.toList())
    }

    val totalDuration by rememberSaveable(segmentDurations) { mutableFloatStateOf(segmentDurations.sum()) }

    val rangeOffset = Command.TenSecondsLeft.duration + Command.Ready.duration
    val range by rememberSaveable(shootingDuration) {
        val range = Pair(
            (rangeOffset + 1),
            (shootingDuration + rangeOffset + Command.CeaseFire.duration - 1).toInt()
        )
        mutableStateOf(range)
    }

    LaunchedEffect(timerRunningState) {
        if (timerRunningState == TimerRunningState.Running) {
            audioManager.playAudioCue(
                audioCues = audioCues,
                currentTime = currentTime,
                playedAudioIndices = playedAudioIndices,
                onAddPlayedAudioIndex = onAddPlayedAudioIndex
            )

            val startTimeMillis = withFrameMillis { it }
            var lastFrameTimeMillis = startTimeMillis
            while (currentTime < totalDuration && isActive) {
                val frameTimeMillis = withFrameMillis { it }
                val deltaTime = (frameTimeMillis - lastFrameTimeMillis) / 1000f
                timerViewModel.setCurrentTime(currentTime + deltaTime)
                audioManager.playAudioCue(
                    audioCues = audioCues,
                    currentTime = currentTime,
                    playedAudioIndices = playedAudioIndices,
                    onAddPlayedAudioIndex = onAddPlayedAudioIndex
                )

                if (currentTime >= totalDuration) {
                    timerViewModel.setCurrentTime(totalDuration)
                    timerViewModel.setTimerState(TimerRunningState.Finished)
                    break
                }
                lastFrameTimeMillis = frameTimeMillis
            }
        }
    }

    LaunchedEffect(shootingDuration) {
        timerViewModel.setThumbValues(thumbValues.filter { it.roundToInt() in range.toIntRange() })
    }

    DisposableEffect(Unit) {
        onDispose {
            audioManager.release()
        }
    }

    val onClickPlayButton: () -> Unit = {
        when (timerRunningState) {
            TimerRunningState.NotStarted -> {
                timerViewModel.setTimerState(TimerRunningState.Running)
            }

            TimerRunningState.Running -> {
                timerViewModel.setTimerState(TimerRunningState.Stopped)
            }

            TimerRunningState.Stopped, TimerRunningState.Finished -> {
                timerViewModel.setCurrentTime(0f)
                timerViewModel.setTimerState(TimerRunningState.NotStarted)
            }
        }
    }

    val statelessSettingsComposable: @Composable () -> Unit = {
        Settings(timerViewModel, range.toIntRange(), clearPlayedAudioIndices, segmentDurations)
    }

    when (LocalConfiguration.current.orientation) {
        Configuration.ORIENTATION_PORTRAIT -> {
            PortraitUI(
                timerViewModel,
                segmentDurations,
                onClickPlayButton,
                timerRunningState,
                statelessSettingsComposable,
                300.dp
            )
        }

        else -> {
            LandscapeUI(
                timerViewModel,
                segmentDurations,
                onClickPlayButton,
                timerRunningState,
                statelessSettingsComposable,
                280.dp
            )
        }
    }
}

private fun Pair<Int, Int>.toIntRange(): IntRange {
    return IntRange(first, second)
}

@Composable
fun LandscapeUI(
    timerViewModel: TimerViewModel,
    segmentDurations: List<Float>,
    onClickPlayButton: () -> Unit,
    timerRunningState: TimerRunningState,
    statelessSettingsComposable: @Composable () -> Unit,
    timerSize: Dp
) {
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
                .padding(start = 8.dp)
                .navigationBarsPadding()
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                ShootTimer(timerViewModel, segmentDurations, timerSize)
                PlayButton(
                    onClickPlayButton = onClickPlayButton,
                    timerRunningState = timerRunningState,
                    timerSize = timerSize
                )
            }

        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxHeight()
                .padding(Paddings.Tiny)
                .navigationBarsPadding()
        ) {
            statelessSettingsComposable()
        }
    }
}

@Composable
fun PortraitUI(
    timerViewModel: TimerViewModel,
    segmentDurations: List<Float>,
    onClickPlayButton: () -> Unit,
    timerRunningState: TimerRunningState,
    statelessSettingsComposable: @Composable () -> Unit,
    timerSize: Dp
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(GrayColor)
            .systemBarsPadding()
    ) {
        Spacer(modifier = Modifier.padding(Paddings.Small))
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            ShootTimer(timerViewModel, segmentDurations, timerSize)
            PlayButton(
                onClickPlayButton = onClickPlayButton,
                timerRunningState = timerRunningState,
                timerSize = timerSize
            )
        }
        Spacer(modifier = Modifier.padding(Paddings.Medium))
        statelessSettingsComposable()
    }
}

@Composable
fun Settings(
    timerViewModel: TimerViewModel,
    range: IntRange,
    onClearPlayedAudioIndices: () -> Unit,
    segmentDurations: List<Float>
) {
    val currentTime by timerViewModel.currentTimeFlow.collectAsState(
        initial = 0f, context = Dispatchers.Main
    )
    val shootingDuration by timerViewModel.shootingDurationFlow.collectAsState(
        initial = 0f, context = Dispatchers.Main
    )
    val timerRunningState by timerViewModel.timerRunningStateFlow.collectAsState(
        initial = TimerRunningState.NotStarted, context = Dispatchers.Main
    )
    val highlightedIndex = calculateHighlightedIndex(currentTime, segmentDurations)

    val setThumbValuesMinusOne = rememberUpdatedState { thumbValues: List<Float> ->
        timerViewModel.setThumbValues(thumbValues.dropLast(1))
    }
    val setThumbValuesPlusOne = rememberUpdatedState { thumbValues: List<Float> ->
        if (thumbValues.size < (range.last - range.first)) {
            timerViewModel.setThumbValues(thumbValues + findNextFreeThumbSpot(range, thumbValues))
        }
    }
    val thumbValues by timerViewModel.thumbValuesFlow.collectAsState(
        initial = listOf(), context = Dispatchers.Main
    )
    val onHorizontalDragSetThumbValues = rememberUpdatedState { newThumbValues: List<Float> ->
        timerViewModel.setThumbValues(newThumbValues)
    }
    val onHorizontalDragRoundThumbValues = rememberUpdatedState {
        timerViewModel.roundThumbValues()
    }

    ShowSegmentTimes(timerViewModel)
    Spacer(modifier = Modifier.padding(Paddings.Small))
    ShootTimeSlider(
        shootingDuration = shootingDuration,
        timerRunningState = timerRunningState,
        onValueChange = { duration ->
            timerViewModel.setShootingTime(duration.roundToInt().toFloat())
            onClearPlayedAudioIndices()
        }
    )
    Spacer(modifier = Modifier.padding(Paddings.Small))
    TicksAdjuster(
        thumbValues = thumbValues,
        range = range,
        setThumbValuesMinusOne = setThumbValuesMinusOne,
        setThumbValuesPlusOne = setThumbValuesPlusOne,
        onHorizontalDragSetThumbValues = onHorizontalDragSetThumbValues,
        onHorizontalDragRoundThumbValues = onHorizontalDragRoundThumbValues
    )
    Spacer(modifier = Modifier.padding(Paddings.Small))
    CommandList(highlightedIndex)
}

private fun calculateHighlightedIndex(currentTime: Float, highlightDurations: List<Float>): Int {
    var accumulatedTime = 0f
    highlightDurations.forEachIndexed { index, duration ->
        accumulatedTime += duration
        if (currentTime < accumulatedTime) {
            return index + 2 // +2 due to the initial commands not being part of the timer duration
        }
    }
    return 7 // Default to the last command if time exceeds all durations
}

private fun findNextFreeThumbSpot(range: IntRange, takenSpots: List<Float>): Float {
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
    tvm.setTimerState(TimerRunningState.NotStarted)

    val segmentDurations = listOf(7f, 3f, 5f, 3f, 4f, 2f)
    val settings: @Composable () -> Unit = {
        Settings(tvm, IntRange(5, 12), { }, segmentDurations)
    }
    PortraitUI(
        tvm,
        segmentDurations,
        {},
        TimerRunningState.NotStarted,
        settings,
        300.dp
    )
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
    tvm.setTimerState(TimerRunningState.NotStarted)

    val segmentDurations = listOf(7f, 3f, 5f, 3f, 4f, 2f)
    val settings: @Composable () -> Unit = {
        Settings(tvm, IntRange(5, 12), { }, segmentDurations)
    }
    LandscapeUI(
        tvm,
        segmentDurations,
        {},
        TimerRunningState.NotStarted,
        settings,
        280.dp
    )
}
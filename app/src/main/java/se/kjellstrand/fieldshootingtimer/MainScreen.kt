package se.kjellstrand.fieldshootingtimer

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
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
import se.kjellstrand.fieldshootingtimer.audio.AudioManager
import se.kjellstrand.fieldshootingtimer.ui.Command
import se.kjellstrand.fieldshootingtimer.ui.CommandList
import se.kjellstrand.fieldshootingtimer.ui.PlayButton
import se.kjellstrand.fieldshootingtimer.ui.ShootTimeAdjuster
import se.kjellstrand.fieldshootingtimer.ui.ShootTimer
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

private const val VIBRATION_LENGTH_MS = 300L

@Composable
fun MainScreen(
    timerViewModel: TimerViewModel
) {
    val context = LocalContext.current

    val systemAudioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager

    var savedCurrentTime by rememberSaveable { mutableFloatStateOf(0f) }
    val currentTime by timerViewModel.currentTimeFlow.collectAsState(
        initial = savedCurrentTime, context = Dispatchers.Main
    )
    savedCurrentTime = currentTime

    val shootingDuration by timerViewModel.shootingDurationFlow.collectAsState(
        initial = 0f, context = Dispatchers.Main
    )

    val timerRunningState by timerViewModel.timerRunningStateFlow.collectAsState(
        initial = TimerRunningState.NotStarted, context = Dispatchers.Main
    )

    var savedThumbValues by rememberSaveable { mutableStateOf<List<Float>>(listOf()) }
    val thumbValues by timerViewModel.thumbValuesFlow.collectAsState(
        initial = savedThumbValues, context = Dispatchers.Main
    )
    savedThumbValues = thumbValues

    var passedThumbs by rememberSaveable { mutableStateOf(setOf<Float>()) }
    val vibrator = remember { context.getSystemService(android.os.Vibrator::class.java) }

    LaunchedEffect(savedCurrentTime, thumbValues) {
        thumbValues.forEach { thumbValue ->
            if (thumbValue !in passedThumbs && savedCurrentTime >= thumbValue) {
                when (systemAudioManager.ringerMode) {
                    android.media.AudioManager.RINGER_MODE_SILENT -> {
                        Log.d("MainScreen", "Skipping vibration because device is in silent mode.")
                    }

                    else -> {
                        val mutablePassedThumbs = passedThumbs.toMutableSet()
                        mutablePassedThumbs.add(thumbValue)
                        passedThumbs = mutablePassedThumbs.toSet()
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            vibrator?.vibrate(
                                android.os.VibrationEffect.createOneShot(
                                    VIBRATION_LENGTH_MS,
                                    android.os.VibrationEffect.DEFAULT_AMPLITUDE
                                )
                            )
                        }
                    }
                }
            }
        }
    }

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

    val audioManager = remember { AudioManager(context) }
    val audioCues by rememberSaveable(timerRunningState) {
        val cues = mutableListOf<Pair<Float, Command>>()
        var time = 0f

        cues.add(Pair(time, Command.TenSecondsLeft))
        time += Command.TenSecondsLeft.duration

        cues.add(Pair(time, Command.Ready))
        time += Command.Ready.duration

        cues.add(Pair(time, Command.Fire))
        time += shootingDuration.toInt().toFloat()

        cues.add(Pair(time, Command.CeaseFire))
        time += Command.CeaseFire.duration

        cues.add(Pair(time, Command.UnloadWeapon))
        time += Command.UnloadWeapon.duration

        cues.add(Pair(time, Command.Visitation))
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
            passedThumbs = setOf()

            audioManager.playAudioCue(
                audioCues = audioCues,
                currentTime = savedCurrentTime,
                playedAudioIndices = playedAudioIndices,
                onAddPlayedAudioIndex = onAddPlayedAudioIndex
            )

            val startTimeMillis = withFrameMillis { it }
            var lastFrameTimeMillis = startTimeMillis
            while (savedCurrentTime < totalDuration && isActive) {
                val frameTimeMillis = withFrameMillis { it }
                val deltaTime = (frameTimeMillis - lastFrameTimeMillis) / 1000f
                timerViewModel.setCurrentTime(savedCurrentTime + deltaTime)
                audioManager.playAudioCue(
                    audioCues = audioCues,
                    currentTime = savedCurrentTime,
                    playedAudioIndices = playedAudioIndices,
                    onAddPlayedAudioIndex = onAddPlayedAudioIndex
                )

                if (savedCurrentTime >= totalDuration) {
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
                clearPlayedAudioIndices()
                timerViewModel.setTimerState(TimerRunningState.NotStarted)
            }
        }
    }

    val statelessSettingsComposable: @Composable () -> Unit = {
        Settings(timerViewModel, range.toIntRange(), segmentDurations)
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Paddings.Large)
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

    val setThumbValuesMinusOne = rememberUpdatedState {
        timerViewModel.dropLastThumbValue()
    }
    val setThumbValuesPlusOne = rememberUpdatedState {
        timerViewModel.addNewThumbValue(range)
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
    val onShootTimeAdjusterValueChange = rememberUpdatedState { duration: List<Float> ->
        val shootingTime = if (duration.isEmpty()) 0f else duration.first().roundToInt().toFloat()
        timerViewModel.setShootingTime(shootingTime)
    }
    val enabled = timerRunningState == TimerRunningState.NotStarted

    Spacer(modifier = Modifier.padding(Paddings.Small))
    ShootTimeAdjuster(
        shootingDuration = shootingDuration,
        enabled = enabled,
        onValueChange = onShootTimeAdjusterValueChange
    )
    Spacer(modifier = Modifier.padding(Paddings.Small))
    TicksAdjuster(
        thumbValues = thumbValues,
        range = range,
        enabled = enabled,
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
            return index + Command.TenSecondsLeft.ordinal
        }
    }
    return Command.Visitation.ordinal
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
        Settings(tvm, IntRange(5, 12), segmentDurations)
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
        Settings(tvm, IntRange(5, 12), segmentDurations)
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
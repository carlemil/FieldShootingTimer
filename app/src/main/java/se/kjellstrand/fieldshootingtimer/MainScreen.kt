package se.kjellstrand.fieldshootingtimer

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import se.kjellstrand.fieldshootingtimer.audio.AudioManager
import se.kjellstrand.fieldshootingtimer.ui.LandscapeLayout
import se.kjellstrand.fieldshootingtimer.ui.PortraitLayout
import se.kjellstrand.fieldshootingtimer.ui.SettingsPanel
import se.kjellstrand.fieldshootingtimer.ui.TimerRunningState
import se.kjellstrand.fieldshootingtimer.ui.TimerViewModel
import kotlin.math.roundToInt

private const val VIBRATION_LENGTH_MS = 300L

@Composable
fun MainScreen(
    timerViewModel: TimerViewModel
) {
    val context = LocalContext.current
    val systemAudioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager

    val shootingDuration by timerViewModel.shootingDurationFlow.collectAsState(
        initial = 0f, context = Dispatchers.Main
    )

    val timerRunningState by timerViewModel.timerRunningStateFlow.collectAsState(
        initial = TimerRunningState.NotStarted, context = Dispatchers.Main
    )

    val thumbValues by timerViewModel.thumbValuesFlow.collectAsState(
        initial = emptyList(), context = Dispatchers.Main
    )

    val segmentDurations by timerViewModel.segmentDurationsFlow.collectAsState(
        initial = emptyList(), context = Dispatchers.Main
    )

    val range by timerViewModel.rangeFlow.collectAsState(
        initial = IntRange.EMPTY, context = Dispatchers.Main
    )

    val audioManager = remember { AudioManager(context) }
    val vibrator = remember { context.getSystemService(android.os.Vibrator::class.java) }

    LaunchedEffect(Unit) {
        timerViewModel.cueEventsFlow.collect { command ->
            audioManager.play(command)
        }
    }

    LaunchedEffect(Unit) {
        timerViewModel.thumbCrossedFlow.collect {
            if (systemAudioManager.ringerMode == android.media.AudioManager.RINGER_MODE_SILENT) {
                Log.d("MainScreen", "Skipping vibration because device is in silent mode.")
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(
                    android.os.VibrationEffect.createOneShot(
                        VIBRATION_LENGTH_MS,
                        android.os.VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            }
        }
    }

    LaunchedEffect(shootingDuration) {
        timerViewModel.setThumbValues(thumbValues.filter { it.roundToInt() in range })
    }

    DisposableEffect(Unit) {
        onDispose {
            audioManager.release()
        }
    }

    val onClickPlayButton: () -> Unit = {
        when (timerRunningState) {
            TimerRunningState.NotStarted -> timerViewModel.start()
            TimerRunningState.Running -> timerViewModel.stop()
            TimerRunningState.Stopped, TimerRunningState.Finished -> timerViewModel.reset()
        }
    }

    val statelessSettingsComposable: @Composable () -> Unit = {
        SettingsPanel(timerViewModel, range, segmentDurations)
    }

    when (LocalConfiguration.current.orientation) {
        Configuration.ORIENTATION_PORTRAIT -> {
            PortraitLayout(
                timerViewModel,
                segmentDurations,
                onClickPlayButton,
                timerRunningState,
                statelessSettingsComposable,
                300.dp
            )
        }

        else -> {
            LandscapeLayout(
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

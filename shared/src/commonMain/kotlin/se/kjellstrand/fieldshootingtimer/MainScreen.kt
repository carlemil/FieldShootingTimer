package se.kjellstrand.fieldshootingtimer

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import se.kjellstrand.fieldshootingtimer.platform.KeepScreenOn
import se.kjellstrand.fieldshootingtimer.platform.rememberAudioPlayer
import se.kjellstrand.fieldshootingtimer.platform.rememberHaptics
import se.kjellstrand.fieldshootingtimer.platform.rememberPlatformAudioPolicy
import se.kjellstrand.fieldshootingtimer.ui.Command
import se.kjellstrand.fieldshootingtimer.ui.LandscapeLayout
import se.kjellstrand.fieldshootingtimer.ui.PortraitLayout
import se.kjellstrand.fieldshootingtimer.ui.SettingsPanel
import se.kjellstrand.fieldshootingtimer.ui.TimerRunningState
import se.kjellstrand.fieldshootingtimer.ui.TimerViewModel

internal fun dispatchPlayButtonClick(
    state: TimerRunningState,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onReset: () -> Unit
) {
    when (state) {
        TimerRunningState.NotStarted -> onStart()
        TimerRunningState.Running -> onStop()
        TimerRunningState.Stopped, TimerRunningState.Finished -> onReset()
    }
}

@Composable
fun MainScreen() {
    val timerViewModel: TimerViewModel = viewModel { TimerViewModel() }

    val timerRunningState by timerViewModel.timerRunningStateFlow.collectAsState(
        initial = TimerRunningState.NotStarted, context = Dispatchers.Main
    )

    val segmentDurations by timerViewModel.segmentDurationsFlow.collectAsState(
        context = Dispatchers.Main
    )

    val range by timerViewModel.rangeFlow.collectAsState(
        context = Dispatchers.Main
    )

    val audioPlayer = rememberAudioPlayer()
    val haptics = rememberHaptics()
    val audioPolicy = rememberPlatformAudioPolicy()

    LaunchedEffect(audioPlayer) {
        audioPlayer.preload(Command.audibleCommands)
    }

    LaunchedEffect(Unit) {
        timerViewModel.cueEventsFlow.collect { command ->
            if (audioPolicy.shouldPlayCue()) {
                audioPlayer.play(command)
            }
        }
    }

    LaunchedEffect(Unit) {
        timerViewModel.thumbCrossedFlow.collect {
            if (audioPolicy.shouldVibrate()) {
                haptics.shortTick()
            }
        }
    }

    KeepScreenOn(enabled = timerRunningState == TimerRunningState.Running)

    val onClickPlayButton: () -> Unit = {
        dispatchPlayButtonClick(
            state = timerRunningState,
            onStart = timerViewModel::start,
            onStop = timerViewModel::stop,
            onReset = timerViewModel::reset
        )
    }

    val statelessSettingsComposable: @Composable () -> Unit = {
        SettingsPanel(timerViewModel, range, segmentDurations)
    }

    BoxWithConstraints {
        val isLandscape = maxWidth > maxHeight
        if (isLandscape) {
            LandscapeLayout(
                timerViewModel,
                segmentDurations,
                onClickPlayButton,
                timerRunningState,
                statelessSettingsComposable,
                280.dp
            )
        } else {
            PortraitLayout(
                timerViewModel,
                segmentDurations,
                onClickPlayButton,
                timerRunningState,
                statelessSettingsComposable,
                300.dp
            )
        }
    }
}

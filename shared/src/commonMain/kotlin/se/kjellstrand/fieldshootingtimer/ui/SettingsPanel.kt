package se.kjellstrand.fieldshootingtimer.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import kotlinx.coroutines.Dispatchers
import se.kjellstrand.fieldshootingtimer.domain.Command
import se.kjellstrand.fieldshootingtimer.domain.TimerMode

/**
 * The command list with the mode-appropriate rows and running highlight.
 * Training hides the competition-only preparation commands; competition
 * shows the full list. Tapping a row seeks the timer to that command's
 * start, pausing any ongoing run ([TimerViewModel.seekTo]).
 */
@Composable
fun SettingsPanel(
    timerViewModel: TimerViewModel,
    segmentDurations: List<Float>
) {
    val currentTime by timerViewModel.currentTimeFlow.collectAsState(
        initial = 0f, context = Dispatchers.Main
    )
    val timerRunningState by timerViewModel.timerRunningStateFlow.collectAsState(
        initial = TimerRunningState.NotStarted, context = Dispatchers.Main
    )
    val timerMode by timerViewModel.timerModeFlow.collectAsState(
        initial = TimerMode.Training, context = Dispatchers.Main
    )
    val awaitingReadyConfirmation by timerViewModel.awaitingReadyConfirmationFlow.collectAsState(
        initial = false, context = Dispatchers.Main
    )
    val ceaseFireBeep by timerViewModel.ceaseFireBeepFlow.collectAsState(
        initial = false, context = Dispatchers.Main
    )

    val visibleCommands = when (timerMode) {
        TimerMode.Competition -> Command.listedCommands
        TimerMode.Training -> Command.listedCommands - Command.Load - Command.AllReady
    }
    val highlighted = highlightedCommand(
        mode = timerMode,
        runningState = timerRunningState,
        currentTime = currentTime,
        segmentDurations = segmentDurations,
        awaitingReadyConfirmation = awaitingReadyConfirmation
    )

    // Owns its column so callers don't have to provide a specific layout.
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        CommandList(
            commands = visibleCommands,
            highlighted = highlighted,
            ceaseFireBeep = ceaseFireBeep,
            onCommandClick = timerViewModel::seekTo
        )
    }
}

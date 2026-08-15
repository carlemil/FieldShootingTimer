package se.kjellstrand.fieldshootingtimer

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import se.kjellstrand.fieldshootingtimer.persistence.rememberSettingsStore
import se.kjellstrand.fieldshootingtimer.platform.KeepScreenOn
import se.kjellstrand.fieldshootingtimer.platform.rememberAudioPlayer
import se.kjellstrand.fieldshootingtimer.platform.rememberHaptics
import se.kjellstrand.fieldshootingtimer.platform.rememberPlatformAudioPolicy
import se.kjellstrand.fieldshootingtimer.platform.rememberSharer
import se.kjellstrand.fieldshootingtimer.domain.Command
import se.kjellstrand.fieldshootingtimer.domain.TimerMode
import se.kjellstrand.fieldshootingtimer.ui.LandscapeLayout
import se.kjellstrand.fieldshootingtimer.ui.MENU_SCRIM_TAG
import se.kjellstrand.fieldshootingtimer.ui.PortraitLayout
import se.kjellstrand.fieldshootingtimer.ui.RadialMenu
import se.kjellstrand.fieldshootingtimer.ui.SettingsPanel
import se.kjellstrand.fieldshootingtimer.ui.TimerRunningState
import se.kjellstrand.fieldshootingtimer.ui.TimerViewModel
import se.kjellstrand.fieldshootingtimer.ui.TutorialOverlay
import se.kjellstrand.fieldshootingtimer.ui.theme.Paddings
import se.kjellstrand.fieldshootingtimer.ui.tutorialSteps

private const val SHARE_URL = "https://carlemil.github.io/FieldShootingTimer/"

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
    val settingsStore = rememberSettingsStore()
    val timerViewModel: TimerViewModel = viewModel { TimerViewModel(settingsStore = settingsStore) }
    MainScreen(timerViewModel)
}

// Seam for host-side UI tests: lets a test seed its own TimerViewModel while
// the public no-arg entry point (used by MainActivity and MainViewController)
// keeps owning the real one.
@Composable
internal fun MainScreen(timerViewModel: TimerViewModel) {
    val timerRunningState by timerViewModel.timerRunningStateFlow.collectAsState(
        initial = TimerRunningState.NotStarted, context = Dispatchers.Main
    )

    val segmentDurations by timerViewModel.segmentDurationsFlow.collectAsState(
        context = Dispatchers.Main
    )

    val timerMode by timerViewModel.timerModeFlow.collectAsState(
        initial = TimerMode.Training, context = Dispatchers.Main
    )

    val audioPlayer = rememberAudioPlayer()
    val haptics = rememberHaptics()
    val audioPolicy = rememberPlatformAudioPolicy()
    val sharer = rememberSharer()

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
        SettingsPanel(timerViewModel, segmentDurations)
    }

    var menuOpen by remember { mutableStateOf(false) }

    // Tutorial: auto-shown once on first launch (persisted via tutorialSeen),
    // reopenable any time from the menu's help item.
    val tutorialSeen by timerViewModel.tutorialSeenFlow.collectAsState(
        initial = true, context = Dispatchers.Main
    )
    var tutorialStep by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(tutorialSeen) {
        if (!tutorialSeen && tutorialStep == null) tutorialStep = 0
    }
    val dismissTutorial: () -> Unit = {
        tutorialStep = null
        timerViewModel.markTutorialSeen()
    }

    BoxWithConstraints {
        val isLandscape = maxWidth > maxHeight
        if (isLandscape) {
            LandscapeLayout(
                timerViewModel = timerViewModel,
                segmentDurations = segmentDurations,
                onClickPlayButton = onClickPlayButton,
                timerRunningState = timerRunningState,
                statelessSettingsComposable = statelessSettingsComposable,
                timerSize = 280.dp
            )
        } else {
            PortraitLayout(
                timerViewModel = timerViewModel,
                segmentDurations = segmentDurations,
                onClickPlayButton = onClickPlayButton,
                timerRunningState = timerRunningState,
                statelessSettingsComposable = statelessSettingsComposable,
                timerSize = 300.dp
            )
        }
        // While the menu is open, a scrim covers (and swallows presses to)
        // everything except the menu itself, which is composed on top of it.
        // Tapping the scrim closes the menu.
        val scrimAlpha by animateFloatAsState(
            targetValue = if (menuOpen) 0.3f else 0f,
            label = "menuScrim"
        )
        if (scrimAlpha > 0.005f) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = scrimAlpha))
                    .pointerInput(Unit) {
                        detectTapGestures { menuOpen = false }
                    }
                    .testTag(MENU_SCRIM_TAG)
            )
        }
        // Top-right in portrait; top-left in landscape so it never overlaps the
        // settings column that fills the right half in landscape. The menu
        // fans its items toward the screen's interior from that corner.
        RadialMenu(
            open = menuOpen,
            onOpenChange = { menuOpen = it },
            timerMode = timerMode,
            modeToggleEnabled = timerRunningState == TimerRunningState.NotStarted,
            onToggleMode = {
                timerViewModel.setTimerMode(
                    if (timerMode == TimerMode.Competition) TimerMode.Training
                    else TimerMode.Competition
                )
            },
            onShare = { sharer.share(SHARE_URL) },
            onShowTutorial = { tutorialStep = 0 },
            openTowardsStart = !isLandscape,
            modifier = Modifier
                .align(if (isLandscape) Alignment.TopStart else Alignment.TopEnd)
                .systemBarsPadding()
                .padding(Paddings.Medium)
        )
        tutorialStep?.let { stepIndex ->
            TutorialOverlay(
                stepIndex = stepIndex,
                onNext = {
                    if (stepIndex == tutorialSteps.lastIndex) dismissTutorial()
                    else tutorialStep = stepIndex + 1
                },
                onSkip = dismissTutorial
            )
        }
    }
}

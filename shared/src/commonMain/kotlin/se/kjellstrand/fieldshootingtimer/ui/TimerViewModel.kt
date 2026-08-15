package se.kjellstrand.fieldshootingtimer.ui

import se.kjellstrand.fieldshootingtimer.domain.Command

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.time.TimeSource
import se.kjellstrand.fieldshootingtimer.domain.COMPETITION_COUNTDOWN_SECONDS
import se.kjellstrand.fieldshootingtimer.domain.TimerMode
import se.kjellstrand.fieldshootingtimer.domain.buildAudioCues
import se.kjellstrand.fieldshootingtimer.domain.buildRange
import se.kjellstrand.fieldshootingtimer.domain.buildSegmentDurations
import se.kjellstrand.fieldshootingtimer.domain.findNextFreeThumbSpot
import se.kjellstrand.fieldshootingtimer.domain.newlyCrossedThumbs
import se.kjellstrand.fieldshootingtimer.domain.newlyPassedIndices
import se.kjellstrand.fieldshootingtimer.persistence.SettingsStore

data class TimerUiState(
    val shootingDuration: Float = 5f,
    val timerRunningState: TimerRunningState = TimerRunningState.NotStarted,
    val currentTime: Float = 0f,
    val thumbValues: List<Float> = listOf(),
    val timerMode: TimerMode = TimerMode.Training
)

enum class TimerRunningState {
    NotStarted,
    Running,
    Stopped,
    Finished
}

// Single process-wide monotonic origin used as the default time source.
// Only ever read via elapsedNow().inWholeMilliseconds, so the absolute value
// is unobservable — what matters is that deltas are consistent.
private val MonotonicStart = TimeSource.Monotonic.markNow()
private fun defaultTimeSourceMs(): Long = MonotonicStart.elapsedNow().inWholeMilliseconds

class TimerViewModel(
    externalScope: CoroutineScope? = null,
    private val tickMs: Long = 16L,
    private val settingsStore: SettingsStore? = null,
    private val timeSourceMs: () -> Long = ::defaultTimeSourceMs
) : ViewModel() {

    private val scope: CoroutineScope = externalScope ?: viewModelScope

    private val _uiState = MutableStateFlow(TimerUiState())
    val uiStateFlow: StateFlow<TimerUiState> = _uiState

    val shootingDurationFlow = uiStateFlow.map { it.shootingDuration }.distinctUntilChanged()
    val currentTimeFlow = uiStateFlow.map { it.currentTime }.distinctUntilChanged()
    val timerRunningStateFlow = uiStateFlow.map { it.timerRunningState }.distinctUntilChanged()
    val thumbValuesFlow = _uiState.map { it.thumbValues }.distinctUntilChanged()
    val timerModeFlow = _uiState.map { it.timerMode }.distinctUntilChanged()

    val segmentDurationsFlow: StateFlow<List<Float>> = _uiState
        .map { buildSegmentDurations(it.shootingDuration) }
        .stateIn(scope, SharingStarted.Eagerly, buildSegmentDurations(_uiState.value.shootingDuration))

    val rangeFlow: StateFlow<IntRange> = _uiState
        .map { buildRange(it.shootingDuration) }
        .stateIn(scope, SharingStarted.Eagerly, buildRange(_uiState.value.shootingDuration))

    // No replay: late subscribers (e.g. the new MainScreen collector after a
    // config change) must not receive past cues, or audio/vibration would
    // re-fire on every rotation. extraBufferCapacity gives slack so tryEmit
    // never drops events while a subscriber is being re-attached.
    private val _cueEventsFlow = MutableSharedFlow<Command>(extraBufferCapacity = 8)
    val cueEventsFlow: SharedFlow<Command> = _cueEventsFlow.asSharedFlow()

    private val _thumbCrossedFlow = MutableSharedFlow<Float>(extraBufferCapacity = 8)
    val thumbCrossedFlow: SharedFlow<Float> = _thumbCrossedFlow.asSharedFlow()

    private val playedCueIndices = mutableSetOf<Int>()
    private val crossedThumbs = mutableSetOf<Float>()
    private var timerJob: Job? = null

    init {
        settingsStore?.let { store ->
            scope.launch {
                val savedShooting = store.loadShootingDuration()
                val savedThumbs = store.loadThumbValues()
                val savedMode = store.loadTimerMode()
                if (savedShooting != null || savedThumbs != null || savedMode != null) {
                    _uiState.update { current ->
                        current.copy(
                            shootingDuration = savedShooting ?: current.shootingDuration,
                            thumbValues = savedThumbs ?: current.thumbValues,
                            timerMode = savedMode ?: current.timerMode
                        )
                    }
                }
            }
        }
    }

    private fun persistShootingDuration(value: Float) {
        settingsStore?.let { store ->
            scope.launch { store.saveShootingDuration(value) }
        }
    }

    private fun persistThumbValues() {
        settingsStore?.let { store ->
            val snapshot = _uiState.value.thumbValues
            scope.launch { store.saveThumbValues(snapshot) }
        }
    }

    fun setShootingTime(shootingDuration: Float) {
        require(shootingDuration >= 0) { "Shooting duration cannot be negative." }
        val newRange = buildRange(shootingDuration)
        _uiState.update {
            it.copy(
                shootingDuration = shootingDuration,
                thumbValues = it.thumbValues.filter { t -> t.roundToInt() in newRange }
            )
        }
        persistShootingDuration(shootingDuration)
        persistThumbValues()
    }

    fun setTimerMode(mode: TimerMode) {
        _uiState.update { it.copy(timerMode = mode) }
        settingsStore?.let { store ->
            scope.launch { store.saveTimerMode(mode) }
        }
    }

    fun setTimerState(timerState: TimerRunningState) {
        _uiState.update { it.copy(timerRunningState = timerState) }
    }

    fun setCurrentTime(currentTime: Float) {
        _uiState.update { it.copy(currentTime = currentTime) }
    }

    fun setThumbValues(thumbValues: List<Float>) {
        _uiState.update { it.copy(thumbValues = thumbValues) }
        persistThumbValues()
    }

    fun dropLastThumbValue() {
        _uiState.value = _uiState.value.copy(
            thumbValues = _uiState.value.thumbValues.dropLast(1)
        )
        persistThumbValues()
    }

    fun addNewThumbValue(range: IntRange) {
        val thumbValues = _uiState.value.thumbValues.toMutableList()
        if (thumbValues.size < (range.last - range.first)) {
            thumbValues.add(findNextFreeThumbSpot(range, thumbValues))
            _uiState.value = _uiState.value.copy(thumbValues = thumbValues)
            persistThumbValues()
        }
    }

    fun roundThumbValues() {
        _uiState.value = _uiState.value.copy(
            thumbValues = _uiState.value.thumbValues.map { it.roundToInt().toFloat() }
        )
        persistThumbValues()
    }

    // --- Timer lifecycle ---

    fun start() {
        if (timerJob?.isActive == true) return
        // Competition prefixes the sequence with a preparation countdown,
        // modeled as currentTime rising from -COMPETITION_COUNTDOWN_SECONDS
        // to 0 — every cue time is >= 0, so the normal loop machinery stays
        // silent until the countdown ends. Seeded only on a fresh start;
        // resuming from Stopped keeps the (possibly negative) stop time.
        if (_uiState.value.timerMode == TimerMode.Competition &&
            _uiState.value.timerRunningState == TimerRunningState.NotStarted &&
            _uiState.value.currentTime == 0f
        ) {
            setCurrentTime(-COMPETITION_COUNTDOWN_SECONDS)
        }
        setTimerState(TimerRunningState.Running)
        timerJob = scope.launch {
            // Snapshot directly from _uiState — stateIn-derived flows may not have
            // propagated the latest shootingDuration when start() is called from a test.
            val shootingDuration = _uiState.value.shootingDuration
            val initialTime = _uiState.value.currentTime
            val segments = buildSegmentDurations(shootingDuration)
            val total = segments.sum()
            val cues = buildAudioCues(shootingDuration)
            val thumbs = _uiState.value.thumbValues

            // Anchor against wall clock so dropped frames or scheduler hiccups
            // don't accumulate drift — currentTime is always (now - epoch).
            val startEpochMs = timeSourceMs() - (initialTime * 1000f).toLong()

            setCurrentTime(initialTime)
            emitPassedCues(initialTime, cues)
            emitPassedThumbs(initialTime, thumbs)

            while (isActive && _uiState.value.timerRunningState == TimerRunningState.Running) {
                delay(tickMs)
                val elapsed = (timeSourceMs() - startEpochMs) / 1000f
                if (elapsed >= total) {
                    setCurrentTime(total)
                    emitPassedCues(total, cues)
                    emitPassedThumbs(total, thumbs)
                    setTimerState(TimerRunningState.Finished)
                    break
                }
                setCurrentTime(elapsed)
                emitPassedCues(elapsed, cues)
                emitPassedThumbs(elapsed, thumbs)
            }
        }
    }

    fun stop() {
        if (_uiState.value.timerRunningState != TimerRunningState.Running) return
        // Stopping during the competition preparation countdown cancels it
        // outright — there is nothing worth resuming mid-countdown.
        if (_uiState.value.currentTime < 0f) {
            reset()
            return
        }
        timerJob?.cancel()
        timerJob = null
        setTimerState(TimerRunningState.Stopped)
    }

    fun reset() {
        timerJob?.cancel()
        timerJob = null
        playedCueIndices.clear()
        crossedThumbs.clear()
        setCurrentTime(0f)
        setTimerState(TimerRunningState.NotStarted)
    }

    private fun emitPassedCues(time: Float, cues: List<Pair<Float, Command>>) {
        newlyPassedIndices(time, cues, playedCueIndices).forEach { index ->
            playedCueIndices.add(index)
            _cueEventsFlow.tryEmit(cues[index].second)
        }
    }

    private fun emitPassedThumbs(time: Float, thumbs: List<Float>) {
        newlyCrossedThumbs(time, thumbs, crossedThumbs).forEach { t ->
            crossedThumbs.add(t)
            _thumbCrossedFlow.tryEmit(t)
        }
    }
}

package se.kjellstrand.fieldshootingtimer.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

data class TimerUiState(
    val shootingDuration: Float = 5f,
    val timerRunningState: TimerRunningState = TimerRunningState.NotStarted,
    val currentTime: Float = 0f,
    val thumbValues: List<Float> = listOf()
)

enum class TimerRunningState {
    NotStarted,
    Running,
    Stopped,
    Finished
}

open class TimerViewModel @JvmOverloads constructor(
    externalScope: CoroutineScope? = null,
    private val tickMs: Long = 16L,
    private val savedStateHandle: SavedStateHandle? = null
) : ViewModel() {

    // Picked up by SavedStateViewModelFactory (the default factory behind
    // `by viewModels()`) because it's a single-arg SavedStateHandle ctor.
    constructor(handle: SavedStateHandle) : this(savedStateHandle = handle)

    private val scope: CoroutineScope = externalScope ?: viewModelScope

    private val _uiState = MutableStateFlow(TimerUiState())
    val uiStateFlow: StateFlow<TimerUiState> = _uiState

    val shootingDurationFlow = uiStateFlow.map { it.shootingDuration }.distinctUntilChanged()
    val currentTimeFlow = uiStateFlow.map { it.currentTime }.distinctUntilChanged()
    val timerRunningStateFlow = uiStateFlow.map { it.timerRunningState }.distinctUntilChanged()
    val thumbValuesFlow = _uiState.map { it.thumbValues }.distinctUntilChanged()

    val segmentDurationsFlow: StateFlow<List<Float>> = _uiState
        .map { buildSegmentDurations(it.shootingDuration) }
        .stateIn(scope, SharingStarted.Eagerly, buildSegmentDurations(_uiState.value.shootingDuration))

    val totalDurationFlow: StateFlow<Float> = segmentDurationsFlow
        .map { it.sum() }
        .stateIn(scope, SharingStarted.Eagerly, segmentDurationsFlow.value.sum())

    val audioCuesFlow: StateFlow<List<Pair<Float, Command>>> = _uiState
        .map { buildAudioCues(it.shootingDuration) }
        .stateIn(scope, SharingStarted.Eagerly, buildAudioCues(_uiState.value.shootingDuration))

    val rangeFlow: StateFlow<IntRange> = _uiState
        .map { buildRange(it.shootingDuration) }
        .stateIn(scope, SharingStarted.Eagerly, buildRange(_uiState.value.shootingDuration))

    // replay > 0 so late subscribers (e.g. after a config change that recreates the
    // composition) can rebuild their played-set from history. resetReplayCache() is
    // called from reset() so a fresh timer run starts with a clean slate.
    private val _cueEventsFlow = MutableSharedFlow<Command>(replay = 8)
    val cueEventsFlow: SharedFlow<Command> = _cueEventsFlow.asSharedFlow()

    private val _thumbCrossedFlow = MutableSharedFlow<Float>(replay = 8)
    val thumbCrossedFlow: SharedFlow<Float> = _thumbCrossedFlow.asSharedFlow()

    private val playedCueIndices = mutableSetOf<Int>()
    private val crossedThumbs = mutableSetOf<Float>()
    private var timerJob: Job? = null

    init {
        savedStateHandle?.let { handle ->
            val savedShooting = handle.get<Float>(KEY_SHOOTING_DURATION)
            val savedThumbs = handle.get<FloatArray>(KEY_THUMB_VALUES)?.toList()
            if (savedShooting != null || savedThumbs != null) {
                _uiState.update { current ->
                    current.copy(
                        shootingDuration = savedShooting ?: current.shootingDuration,
                        thumbValues = savedThumbs ?: current.thumbValues
                    )
                }
            }
        }
    }

    fun setShootingTime(shootingDuration: Float) {
        require(shootingDuration >= 0) { "Shooting duration cannot be negative." }
        _uiState.update { it.copy(shootingDuration = shootingDuration) }
        savedStateHandle?.set(KEY_SHOOTING_DURATION, shootingDuration)
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

    private fun persistThumbValues() {
        savedStateHandle?.set(KEY_THUMB_VALUES, _uiState.value.thumbValues.toFloatArray())
    }

    // --- Timer lifecycle ---

    fun start() {
        if (timerJob?.isActive == true) return
        setTimerState(TimerRunningState.Running)
        timerJob = scope.launch {
            // Snapshot directly from _uiState — stateIn-derived flows may not have
            // propagated the latest shootingDuration when start() is called from a test.
            val shootingDuration = _uiState.value.shootingDuration
            var currentTime = _uiState.value.currentTime
            val segments = buildSegmentDurations(shootingDuration)
            val total = segments.sum()
            val cues = buildAudioCues(shootingDuration)
            val thumbs = _uiState.value.thumbValues

            setCurrentTime(currentTime)
            emitPassedCues(currentTime, cues)
            emitPassedThumbs(currentTime, thumbs)

            while (isActive && _uiState.value.timerRunningState == TimerRunningState.Running) {
                delay(tickMs)
                currentTime += tickMs / 1000f
                if (currentTime >= total) {
                    setCurrentTime(total)
                    emitPassedCues(total, cues)
                    emitPassedThumbs(total, thumbs)
                    setTimerState(TimerRunningState.Finished)
                    break
                }
                setCurrentTime(currentTime)
                emitPassedCues(currentTime, cues)
                emitPassedThumbs(currentTime, thumbs)
            }
        }
    }

    fun stop() {
        if (_uiState.value.timerRunningState != TimerRunningState.Running) return
        timerJob?.cancel()
        timerJob = null
        setTimerState(TimerRunningState.Stopped)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun reset() {
        timerJob?.cancel()
        timerJob = null
        playedCueIndices.clear()
        crossedThumbs.clear()
        _cueEventsFlow.resetReplayCache()
        _thumbCrossedFlow.resetReplayCache()
        setCurrentTime(0f)
        setTimerState(TimerRunningState.NotStarted)
    }

    private fun emitPassedCues(time: Float, cues: List<Pair<Float, Command>>) {
        cues.forEachIndexed { index, (cueTime, cmd) ->
            if (time >= cueTime && index !in playedCueIndices) {
                playedCueIndices.add(index)
                _cueEventsFlow.tryEmit(cmd)
            }
        }
    }

    private fun emitPassedThumbs(time: Float, thumbs: List<Float>) {
        thumbs.forEach { t ->
            if (time >= t && t !in crossedThumbs) {
                crossedThumbs.add(t)
                _thumbCrossedFlow.tryEmit(t)
            }
        }
    }

    // --- Helpers ---

    private fun findNextFreeThumbSpot(range: IntRange, thumbValues: List<Float>): Float {
        val center = (range.first + range.last) / 2
        val maxDistance = (range.last - range.first) / 2
        for (distance in 0..maxDistance) {
            val forward = center + distance
            val backward = center - distance
            if (forward in range && thumbValues.find { it.roundToInt() == forward } == null) {
                return forward.toFloat()
            }
            if (backward in range && thumbValues.find { it.roundToInt() == backward } == null) {
                return backward.toFloat()
            }
        }
        return center.toFloat()
    }

    companion object {
        private const val KEY_SHOOTING_DURATION = "shooting_duration"
        private const val KEY_THUMB_VALUES = "thumb_values"

        private fun buildSegmentDurations(shootingDuration: Float): List<Float> =
            Command.timedCommands.map {
                if (it == Command.Fire) shootingDuration else it.duration.toFloat()
            }

        private fun buildAudioCues(shootingDuration: Float): List<Pair<Float, Command>> {
            val cues = mutableListOf<Pair<Float, Command>>()
            var time = 0f
            cues.add(time to Command.TenSecondsLeft); time += Command.TenSecondsLeft.duration
            cues.add(time to Command.Ready); time += Command.Ready.duration
            cues.add(time to Command.Fire); time += shootingDuration.toInt().toFloat()
            cues.add(time to Command.CeaseFire); time += Command.CeaseFire.duration
            cues.add(time to Command.UnloadWeapon); time += Command.UnloadWeapon.duration
            cues.add(time to Command.Visitation)
            return cues
        }

        private fun buildRange(shootingDuration: Float): IntRange {
            val offset = Command.TenSecondsLeft.duration + Command.Ready.duration
            return IntRange(
                offset + 1,
                (shootingDuration + offset + Command.CeaseFire.duration - 1).toInt()
            )
        }
    }
}

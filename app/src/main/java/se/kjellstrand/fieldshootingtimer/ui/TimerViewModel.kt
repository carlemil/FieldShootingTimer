package se.kjellstrand.fieldshootingtimer.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
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

open class TimerViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(TimerUiState())
    val uiStateFlow: StateFlow<TimerUiState> = _uiState

    val shootingDurationFlow = uiStateFlow.map { it.shootingDuration }.distinctUntilChanged()
    val currentTimeFlow = uiStateFlow.map { it.currentTime }.distinctUntilChanged()
    val timerRunningStateFlow = uiStateFlow.map { it.timerRunningState }.distinctUntilChanged()
    val thumbValuesFlow = _uiState.map { it.thumbValues }.distinctUntilChanged()

    fun setShootingTime(shootingDuration: Float) {
        require(shootingDuration >= 0) { "Shooting duration cannot be negative." }
        _uiState.update { currentState ->
            currentState.copy(shootingDuration = shootingDuration)
        }
    }

    fun setTimerState(timerState: TimerRunningState) {
        _uiState.update { currentState ->
            currentState.copy(timerRunningState = timerState)
        }
    }

    fun setCurrentTime(currentTime: Float) {
        _uiState.update { currentState ->
            currentState.copy(currentTime = currentTime)
        }
    }

    fun setThumbValues(thumbValues: List<Float>) {
        _uiState.update { currentState ->
            currentState.copy(thumbValues = thumbValues)
        }
    }

    fun dropLastThumbValue() {
        _uiState.value = _uiState.value.copy(
            thumbValues = _uiState.value.thumbValues.dropLast(1)
        )
    }

    fun addNewThumbValue(range: IntRange) {
        val thumbValues = _uiState.value.thumbValues.toMutableList()
        if (thumbValues.size < (range.last - range.first)) {
            thumbValues.add(findNextFreeThumbSpot(range, thumbValues))
            _uiState.value = _uiState.value.copy(
                thumbValues = thumbValues
            )
        }
    }

    fun roundThumbValues() {
        _uiState.value = _uiState.value.copy(
            thumbValues = _uiState.value.thumbValues.map { it.roundToInt().toFloat() }
        )
    }

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
}
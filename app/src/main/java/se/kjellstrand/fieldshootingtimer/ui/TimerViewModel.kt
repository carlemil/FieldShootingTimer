package se.kjellstrand.fieldshootingtimer.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

open class TimerViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(TimerUiState())
    val uiState: StateFlow<TimerUiState> = _uiState.asStateFlow()

    val thumbValuesFlow = uiState.map { it.thumbValues }.distinctUntilChanged()
    val shootingDurationFlow = uiState.map { it.shootingDuration }.distinctUntilChanged()
    val timerRunningStateFlow = uiState.map { it.timerRunningState }.distinctUntilChanged()

    init {
        _uiState.value = TimerUiState()
    }

    fun setShootingTime(shootingDuration: Float) {
        _uiState.update { currentState ->
            currentState.copy(shootingDuration = shootingDuration)
        }
    }

    fun setBadgesVisible(badgesVisible: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(badgesVisible = badgesVisible)
        }
    }

    fun setTimerState(timerState: TimerState) {
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
        println("setThumbValues: $thumbValues")
        _uiState.value = _uiState.value.copy(
            thumbValues = thumbValues
        )
    }
}
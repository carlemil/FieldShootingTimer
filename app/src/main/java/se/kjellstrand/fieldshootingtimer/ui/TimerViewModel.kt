package se.kjellstrand.fieldshootingtimer.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

open class TimerViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(TimerUiState())
    val uiState: StateFlow<TimerUiState> = _uiState.asStateFlow()

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

    fun setThumbValues(updatedValues: List<Float>) {
            println("setThumbValues thumbValues: $updatedValues")
        _uiState.value = _uiState.value.copy(thumbValues = updatedValues)
        println("setThumbValues thumbValues: ${_uiState.value.thumbValues}")

//        _uiState.update { currentState ->
//            println("setThumbValues thumbValues: $thumbValues")
//            val a =  currentState.copy(thumbValues = thumbValues)
//            println("uiState setThumbValues thumbValues: ${uiState.value.thumbValues}")
//            a
//        }
    }
}
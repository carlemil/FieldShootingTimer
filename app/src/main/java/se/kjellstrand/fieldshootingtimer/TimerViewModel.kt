package se.kjellstrand.fieldshootingtimer

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

    fun setShootingTime(shootingTime: Float) {
        _uiState.update { currentState ->
            currentState.copy(shootingTime = shootingTime)
        }
    }

    fun setBadgesVisible(badgesVisible: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(badgesVisible = badgesVisible)
        }
    }
}
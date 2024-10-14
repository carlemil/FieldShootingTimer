package se.kjellstrand.fieldshootingtimer

data class TimerUiState(
    val shootingDuration: Float = 5f,
    val badgesVisible: Boolean = false,
    val timerRunningState: TimerState = TimerState.NotStarted,
    val currentTime: Float = 0f
)

enum class TimerState{
    NotStarted,
    Running,
    Stopped,
    Finished
}
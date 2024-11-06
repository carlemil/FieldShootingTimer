package se.kjellstrand.fieldshootingtimer.ui

data class TimerUiState(
    val shootingDuration: Float = 5f,
    val badgesVisible: Boolean = false,
    val timerRunningState: TimerState = TimerState.NotStarted,
    val currentTime: Float = 0f,
    val thumbValues: List<Float> = listOf()
)

enum class TimerState{
    NotStarted,
    Running,
    Stopped,
    Finished
}
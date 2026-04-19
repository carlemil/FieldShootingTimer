package se.kjellstrand.fieldshootingtimer.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.Dispatchers
import se.kjellstrand.fieldshootingtimer.ui.theme.Paddings
import kotlin.math.roundToInt

@Composable
fun SettingsPanel(
    timerViewModel: TimerViewModel,
    range: IntRange,
    segmentDurations: List<Float>
) {
    val currentTime by timerViewModel.currentTimeFlow.collectAsState(
        initial = 0f, context = Dispatchers.Main
    )
    val shootingDuration by timerViewModel.shootingDurationFlow.collectAsState(
        initial = 0f, context = Dispatchers.Main
    )
    val timerRunningState by timerViewModel.timerRunningStateFlow.collectAsState(
        initial = TimerRunningState.NotStarted, context = Dispatchers.Main
    )
    val highlightedIndex = calculateHighlightedIndex(currentTime, segmentDurations)

    val setThumbValuesMinusOne: () -> Unit = {
        timerViewModel.dropLastThumbValue()
    }
    val setThumbValuesPlusOne: () -> Unit = {
        timerViewModel.addNewThumbValue(range)
    }
    val thumbValues by timerViewModel.thumbValuesFlow.collectAsState(
        initial = listOf(), context = Dispatchers.Main
    )
    val onHorizontalDragSetThumbValues: (List<Float>) -> Unit = { newThumbValues ->
        timerViewModel.setThumbValues(newThumbValues)
    }
    val onHorizontalDragRoundThumbValues: () -> Unit = {
        timerViewModel.roundThumbValues()
    }
    val onShootTimeAdjusterValueChange: (List<Float>) -> Unit = { duration ->
        val shootingTime = if (duration.isEmpty()) 0f else duration.first().roundToInt().toFloat()
        timerViewModel.setShootingTime(shootingTime)
    }
    val enabled = timerRunningState == TimerRunningState.NotStarted

    Spacer(modifier = Modifier.padding(Paddings.Small))
    ShootTimeAdjuster(
        shootingDuration = shootingDuration,
        enabled = enabled,
        onValueChange = onShootTimeAdjusterValueChange
    )
    Spacer(modifier = Modifier.padding(Paddings.Small))
    TicksAdjuster(
        thumbValues = thumbValues,
        range = range,
        enabled = enabled,
        setThumbValuesMinusOne = setThumbValuesMinusOne,
        setThumbValuesPlusOne = setThumbValuesPlusOne,
        onHorizontalDragSetThumbValues = onHorizontalDragSetThumbValues,
        onHorizontalDragRoundThumbValues = onHorizontalDragRoundThumbValues
    )
    Spacer(modifier = Modifier.padding(Paddings.Small))
    CommandList(highlightedIndex)
}

private fun calculateHighlightedIndex(currentTime: Float, highlightDurations: List<Float>): Int {
    var accumulatedTime = 0f
    highlightDurations.forEachIndexed { index, duration ->
        accumulatedTime += duration
        if (currentTime < accumulatedTime) {
            return index + Command.TenSecondsLeft.ordinal
        }
    }
    return Command.Visitation.ordinal
}

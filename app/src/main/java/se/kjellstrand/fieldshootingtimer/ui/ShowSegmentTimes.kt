package se.kjellstrand.fieldshootingtimer.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import se.kjellstrand.fieldshootingtimer.R
import se.kjellstrand.fieldshootingtimer.ui.theme.Paddings

@Composable
fun ShowSegmentTimes(
    timerViewModel: TimerViewModel
) {
    val badgesVisible by timerViewModel.badgesVisibleFlow.collectAsState(
        initial = false, context = Dispatchers.Main
    )
    val onCheckedChange = rememberUpdatedState { checked: Boolean ->
        timerViewModel.setBadgesVisible(checked)
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = Paddings.Tiny, end = Paddings.Small)
    ) {
        Checkbox(
            checked = badgesVisible,
            onCheckedChange = { checked -> onCheckedChange.value(checked) }
        )
        Text(
            text = stringResource(R.string.show_segment_times),
            modifier = Modifier.padding(start = Paddings.Small)
        )
    }
}
package se.kjellstrand.fieldshootingtimer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import se.kjellstrand.fieldshootingtimer.ui.theme.BlackColor
import se.kjellstrand.fieldshootingtimer.ui.theme.GrayColor
import se.kjellstrand.fieldshootingtimer.ui.theme.Paddings
import se.kjellstrand.fieldshootingtimer.ui.theme.PaleGreenColor
import se.kjellstrand.fieldshootingtimer.ui.theme.WhiteColor

@Composable
fun CommandList(
    hlIndex: Int
) {
    val commands = Command.entries
    val listState = rememberLazyListState()

    LaunchedEffect(hlIndex) {
        val centerPosition =
            maxOf(0, hlIndex - listState.layoutInfo.visibleItemsInfo.size / 2)
        listState.animateScrollToItem(centerPosition)
    }
    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(Paddings.Medium),
        modifier = Modifier
            .fillMaxWidth()
            .padding(Paddings.Large)
            .border(Paddings.Tiny, BlackColor)
            .background(WhiteColor)
    ) {
        items(commands.size) { index ->
            Text(
                // TODO: real Compose-MP stringResource wired in shared/compose-resources.
                text = commands[index].name,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { selected = (index == hlIndex) }
                    .background(if (index == hlIndex) PaleGreenColor else Color.Transparent)
                    .padding(Paddings.Small),
                color = if (index == hlIndex) BlackColor else GrayColor,
                style = if (index == hlIndex) {
                    MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                } else {
                    MaterialTheme.typography.bodyLarge
                }
            )
        }
    }
}

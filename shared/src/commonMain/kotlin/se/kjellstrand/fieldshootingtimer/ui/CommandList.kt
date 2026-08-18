package se.kjellstrand.fieldshootingtimer.ui

import se.kjellstrand.fieldshootingtimer.domain.Command

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.resources.stringResource
import se.kjellstrand.fieldshootingtimer.ui.theme.BlackColor
import se.kjellstrand.fieldshootingtimer.ui.theme.GrayColor
import se.kjellstrand.fieldshootingtimer.ui.theme.Paddings
import se.kjellstrand.fieldshootingtimer.ui.theme.PaleGreenColor
import se.kjellstrand.fieldshootingtimer.ui.theme.WhiteColor

// Row tags are "$COMMAND_LIST_ROW_TAG${command.name}" — stable even when the
// displayed command list is mode-filtered.
internal const val COMMAND_LIST_ROW_TAG = "CommandListRow"

@Composable
fun CommandList(
    commands: List<Command>,
    highlighted: Command?,
    onCommandClick: (Command) -> Unit = {}
) {
    val listState = rememberLazyListState()
    val hlIndex = commands.indexOf(highlighted)

    LaunchedEffect(hlIndex) {
        val centerPosition =
            maxOf(0, hlIndex - listState.layoutInfo.visibleItemsInfo.size / 2)
        listState.animateScrollToItem(centerPosition)
    }
    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(Paddings.Medium),
        // SpaceEvenly spreads the rows over the full height when they fit;
        // when they don't (small landscape screens) it degrades to a plain
        // scrolling list, which the highlight auto-scroll depends on.
        verticalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier
            .fillMaxSize()
            .padding(Paddings.Large)
            .border(Paddings.Tiny, BlackColor)
            .background(WhiteColor)
    ) {
        items(commands.size) { index ->
            val command = commands[index]
            val isHighlighted = command == highlighted
            Text(
                text = stringResource(command.stringRes),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("$COMMAND_LIST_ROW_TAG${command.name}")
                    .semantics { selected = isHighlighted }
                    .clickable { onCommandClick(command) }
                    .background(if (isHighlighted) PaleGreenColor else Color.Transparent)
                    .padding(Paddings.Small),
                color = if (isHighlighted) BlackColor else GrayColor,
                style = if (isHighlighted) {
                    MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                } else {
                    MaterialTheme.typography.bodyLarge
                }
            )
        }
    }
}

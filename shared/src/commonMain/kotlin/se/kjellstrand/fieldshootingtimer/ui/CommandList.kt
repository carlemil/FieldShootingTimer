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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import se.kjellstrand.fieldshootingtimer.resources.Res
import se.kjellstrand.fieldshootingtimer.resources.command_beep
import se.kjellstrand.fieldshootingtimer.ui.theme.Paddings

// Row tags are "$COMMAND_LIST_ROW_TAG${command.name}" — stable even when the
// displayed command list is mode-filtered.
internal const val COMMAND_LIST_ROW_TAG = "CommandListRow"

@Composable
fun CommandList(
    commands: List<Command>,
    highlighted: Command?,
    // With the beep setting on, the CeaseFire row reads "BEEP!" — that run
    // ends with the signal instead of the spoken command.
    ceaseFireBeep: Boolean = false,
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
            .clip(RoundedCornerShape(8.dp))
            .border(Paddings.Tiny, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
    ) {
        items(commands.size) { index ->
            val command = commands[index]
            val isHighlighted = command == highlighted
            Text(
                text = if (ceaseFireBeep && command == Command.CeaseFire) {
                    stringResource(Res.string.command_beep)
                } else {
                    stringResource(command.stringRes)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("$COMMAND_LIST_ROW_TAG${command.name}")
                    .semantics { selected = isHighlighted }
                    .clickable { onCommandClick(command) }
                    // Half the list box's own 8dp corner radius.
                    .background(
                        if (isHighlighted) MaterialTheme.colorScheme.secondaryContainer
                        else Color.Transparent,
                        RoundedCornerShape(4.dp)
                    )
                    .padding(Paddings.Small),
                color = if (isHighlighted) MaterialTheme.colorScheme.onSecondaryContainer
                else MaterialTheme.colorScheme.onSurfaceVariant,
                style = if (isHighlighted) {
                    MaterialTheme.typography.titleLarge.copy(
                        fontSize = 26.sp,
                        lineHeight = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 20.sp,
                        lineHeight = 26.sp
                    )
                }
            )
        }
    }
}

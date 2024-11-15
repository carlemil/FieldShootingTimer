package se.kjellstrand.fieldshootingtimer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(Paddings.Large)
            .border(Paddings.Tiny, BlackColor)
            .background(WhiteColor)
    ) {
        items(commands.size) { index ->
            Text(
                text = stringResource(commands[index].stringResId),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (index == hlIndex) PaleGreenColor else Color.Transparent)
                    .padding(Paddings.Small),
                color = if (index == hlIndex) BlackColor else GrayColor,
                style = if (index == hlIndex) {
                    MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                } else {
                    MaterialTheme.typography.bodyLarge
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CommandList() {
    CommandList(3)
}
package se.kjellstrand.fieldshootingtimer.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import se.kjellstrand.fieldshootingtimer.resources.Res
import se.kjellstrand.fieldshootingtimer.resources.play_action
import se.kjellstrand.fieldshootingtimer.resources.play_arrow
import se.kjellstrand.fieldshootingtimer.resources.reset_action
import se.kjellstrand.fieldshootingtimer.resources.skip_previous
import se.kjellstrand.fieldshootingtimer.resources.stop
import se.kjellstrand.fieldshootingtimer.resources.stop_action
import se.kjellstrand.fieldshootingtimer.ui.theme.BlackColor
import se.kjellstrand.fieldshootingtimer.ui.theme.Paddings
import se.kjellstrand.fieldshootingtimer.ui.theme.WhiteColor

internal const val PLAY_BUTTON_TAG = "PlayButton"
internal const val PLAY_ICON_TAG = "PlayButtonIconPlay"
internal const val STOP_ICON_TAG = "PlayButtonIconStop"
internal const val RESET_ICON_TAG = "PlayButtonIconReset"
internal const val COUNTDOWN_TEXT_TAG = "CountdownText"

/**
 * While [countdownSeconds] is non-null — the competition-mode preparation
 * countdown, or the shooting stretch's remaining seconds — the button shows
 * the digits beneath a shrunken state icon; tapping it behaves as the icon
 * says.
 */
@Composable
fun PlayButton(
    onClickPlayButton: () -> Unit,
    timerRunningState: TimerRunningState,
    timerSize: Dp,
    countdownSeconds: Int? = null
) {
    Box(
        contentAlignment = Alignment.Center
    ) {
        val buttonSize = timerSize / 3f
        OutlinedButton(
            onClick = onClickPlayButton,
            modifier = Modifier
                .size(buttonSize)
                .testTag(PLAY_BUTTON_TAG),
            shape = CircleShape,
            contentPadding = PaddingValues(0.dp),
            border = BorderStroke(Paddings.Tiny, BlackColor),
            // Same green in every state — a white countdown background made
            // the stop icon nearly invisible. Content is pinned white by
            // explicit design preference (the black onPrimary cleared WCAG
            // better on the light green, but looked worse); the countdown
            // digits inherit the same white.
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = WhiteColor
            )
        ) {
            val icon = when (timerRunningState) {
                TimerRunningState.NotStarted ->
                    Triple(Res.drawable.play_arrow, Res.string.play_action, PLAY_ICON_TAG)

                TimerRunningState.Running ->
                    Triple(Res.drawable.stop, Res.string.stop_action, STOP_ICON_TAG)

                TimerRunningState.Finished, TimerRunningState.Stopped ->
                    Triple(Res.drawable.skip_previous, Res.string.reset_action, RESET_ICON_TAG)
            }
            val (iconRes, iconDescription, iconTag) = icon
            if (countdownSeconds != null) {
                // The state icon shrinks and stays above the digits, so the
                // tap target still reads as play/stop/reset while counting.
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = stringResource(iconDescription),
                        modifier = Modifier
                            .size(buttonSize * 0.35f)
                            .testTag(iconTag)
                    )
                    Text(
                        text = countdownSeconds.toString(),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.testTag(COUNTDOWN_TEXT_TAG)
                    )
                }
            } else {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = stringResource(iconDescription),
                    modifier = Modifier
                        .size(buttonSize * 0.8f)
                        .testTag(iconTag)
                )
            }
        }
    }
}

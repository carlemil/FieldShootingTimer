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
 * While [countdownSeconds] is non-null (competition-mode preparation
 * countdown) the button shows the remaining seconds instead of an icon;
 * tapping it still stops the run.
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
            if (countdownSeconds != null) {
                // Stop icon stays visible above the digits: the tap target
                // reads as "stop" (it cancels the countdown).
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painter = painterResource(Res.drawable.stop),
                        contentDescription = stringResource(Res.string.stop_action),
                        modifier = Modifier
                            .size(buttonSize * 0.35f)
                            .testTag(STOP_ICON_TAG)
                    )
                    Text(
                        text = countdownSeconds.toString(),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.testTag(COUNTDOWN_TEXT_TAG)
                    )
                }
            } else when (timerRunningState) {
                TimerRunningState.NotStarted -> {
                    Icon(
                        painter = painterResource(Res.drawable.play_arrow),
                        contentDescription = stringResource(Res.string.play_action),
                        modifier = Modifier
                            .size(buttonSize * 0.8f)
                            .testTag(PLAY_ICON_TAG)
                    )
                }

                TimerRunningState.Running -> {
                    Icon(
                        painter = painterResource(Res.drawable.stop),
                        contentDescription = stringResource(Res.string.stop_action),
                        modifier = Modifier
                            .size(buttonSize * 0.8f)
                            .testTag(STOP_ICON_TAG)
                    )
                }

                TimerRunningState.Finished, TimerRunningState.Stopped -> {
                    Icon(
                        painter = painterResource(Res.drawable.skip_previous),
                        contentDescription = stringResource(Res.string.reset_action),
                        modifier = Modifier
                            .size(buttonSize * 0.8f)
                            .testTag(RESET_ICON_TAG)
                    )
                }
            }
        }
    }
}

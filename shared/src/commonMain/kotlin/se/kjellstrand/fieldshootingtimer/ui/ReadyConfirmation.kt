package se.kjellstrand.fieldshootingtimer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import se.kjellstrand.fieldshootingtimer.resources.Res
import se.kjellstrand.fieldshootingtimer.resources.ready_confirm_again
import se.kjellstrand.fieldshootingtimer.resources.ready_confirm_continue
import se.kjellstrand.fieldshootingtimer.resources.ready_confirm_question
import se.kjellstrand.fieldshootingtimer.ui.theme.BlackColor
import se.kjellstrand.fieldshootingtimer.ui.theme.Paddings

internal const val READY_CONFIRM_TAG = "ReadyConfirmOverlay"
internal const val READY_CONTINUE_TAG = "ReadyConfirmContinue"
internal const val READY_AGAIN_TAG = "ReadyConfirmAgain"

/**
 * Modal question shown when the competition countdown reaches 0:
 * "Alla klara!". [onContinue] runs the timed sequence from 0;
 * [onAskAgain] re-runs the AllReady stretch of the countdown. The scrim
 * swallows all presses, so answering is the only way forward.
 */
@Composable
internal fun ReadyConfirmationOverlay(
    onContinue: () -> Unit,
    onAskAgain: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .background(BlackColor.copy(alpha = 0.6f))
            .pointerInput(Unit) { detectTapGestures { } }
            .testTag(READY_CONFIRM_TAG)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(Paddings.Large)
        ) {
            Text(
                text = stringResource(Res.string.ready_confirm_question),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = Paddings.Large)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(Res.string.ready_confirm_again),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .testTag(READY_AGAIN_TAG)
                        .clickable { onAskAgain() }
                        .padding(Paddings.Small)
                )
                Button(
                    onClick = onContinue,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.testTag(READY_CONTINUE_TAG)
                ) {
                    Text(text = stringResource(Res.string.ready_confirm_continue))
                }
            }
        }
    }
}

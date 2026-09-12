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
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import se.kjellstrand.fieldshootingtimer.resources.Res
import se.kjellstrand.fieldshootingtimer.resources.load_confirm_question
import se.kjellstrand.fieldshootingtimer.resources.mark_confirm_close
import se.kjellstrand.fieldshootingtimer.resources.mark_confirm_mark
import se.kjellstrand.fieldshootingtimer.resources.mark_confirm_question
import se.kjellstrand.fieldshootingtimer.resources.ready_confirm_continue
import se.kjellstrand.fieldshootingtimer.resources.ready_confirm_question
import se.kjellstrand.fieldshootingtimer.resources.unload_confirm_question
import se.kjellstrand.fieldshootingtimer.resources.visitation_done_confirm_action
import se.kjellstrand.fieldshootingtimer.resources.visitation_done_confirm_question
import se.kjellstrand.fieldshootingtimer.ui.theme.BlackColor
import se.kjellstrand.fieldshootingtimer.ui.theme.Paddings

internal const val LOAD_CONFIRM_TAG = "LoadConfirmOverlay"
internal const val LOAD_CONTINUE_TAG = "LoadConfirmContinue"
internal const val LOAD_CLOSE_TAG = "LoadConfirmClose"
internal const val READY_CONFIRM_TAG = "ReadyConfirmOverlay"
internal const val READY_CONTINUE_TAG = "ReadyConfirmContinue"
internal const val READY_CLOSE_TAG = "ReadyConfirmClose"
internal const val UNLOAD_CONFIRM_TAG = "UnloadConfirmOverlay"
internal const val UNLOAD_CONTINUE_TAG = "UnloadConfirmContinue"
internal const val UNLOAD_CLOSE_TAG = "UnloadConfirmClose"
internal const val MARK_CONFIRM_TAG = "MarkConfirmOverlay"
internal const val MARK_CONFIRM_MARK_TAG = "MarkConfirmMark"
internal const val MARK_CONFIRM_CLOSE_TAG = "MarkConfirmClose"
internal const val VISITATION_DONE_CONFIRM_TAG = "VisitationDoneConfirmOverlay"
internal const val VISITATION_DONE_CONFIRM_ACTION_TAG = "VisitationDoneConfirmAction"
internal const val VISITATION_DONE_CONFIRM_CLOSE_TAG = "VisitationDoneConfirmClose"

/**
 * Modal question shown when play is pressed in competition mode (or the
 * Ladda row is tapped): [onContinue] makes the "Ladda!" call — the caller
 * then asks "Alla klara?" — and [onClose] closes without calling.
 */
@Composable
internal fun LoadConfirmationOverlay(
    onContinue: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) = CallConfirmationOverlay(
    question = Res.string.load_confirm_question,
    confirmText = Res.string.ready_confirm_continue,
    overlayTag = LOAD_CONFIRM_TAG,
    confirmTag = LOAD_CONTINUE_TAG,
    closeTag = LOAD_CLOSE_TAG,
    onConfirm = onContinue,
    onClose = onClose,
    modifier = modifier
)

/**
 * Modal question after "Ladda!" (or when the Alla klara row is tapped):
 * [onContinue] makes the "Alla klara!" call and starts the timed sequence,
 * [onClose] closes without calling.
 */
@Composable
internal fun ReadyConfirmationOverlay(
    onContinue: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) = CallConfirmationOverlay(
    question = Res.string.ready_confirm_question,
    confirmText = Res.string.ready_confirm_continue,
    overlayTag = READY_CONFIRM_TAG,
    confirmTag = READY_CONTINUE_TAG,
    closeTag = READY_CLOSE_TAG,
    onConfirm = onContinue,
    onClose = onClose,
    modifier = modifier
)

/**
 * Modal question when the run reaches the end of CeaseFire (or the row is
 * tapped): [onContinue] makes the "Patron ur!" call and runs on,
 * [onClose] closes without calling.
 */
@Composable
internal fun UnloadConfirmationOverlay(
    onContinue: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) = CallConfirmationOverlay(
    question = Res.string.unload_confirm_question,
    confirmText = Res.string.ready_confirm_continue,
    overlayTag = UNLOAD_CONFIRM_TAG,
    confirmTag = UNLOAD_CONTINUE_TAG,
    closeTag = UNLOAD_CLOSE_TAG,
    onConfirm = onContinue,
    onClose = onClose,
    modifier = modifier
)

/**
 * Modal question shown when the Markera row is tapped (or the visitation-
 * done dialog is confirmed): [onMark] makes the "Markera!" call and closes,
 * [onClose] closes without calling.
 */
@Composable
internal fun MarkConfirmationOverlay(
    onMark: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) = CallConfirmationOverlay(
    question = Res.string.mark_confirm_question,
    confirmText = Res.string.mark_confirm_mark,
    overlayTag = MARK_CONFIRM_TAG,
    confirmTag = MARK_CONFIRM_MARK_TAG,
    closeTag = MARK_CONFIRM_CLOSE_TAG,
    onConfirm = onMark,
    onClose = onClose,
    modifier = modifier
)

/**
 * Modal question shown when a competition run finishes (or the row is
 * tapped): [onConfirm] makes the "Visitation klar!" call — the caller then
 * hands over to the Markera dialog — and [onClose] closes without calling.
 */
@Composable
internal fun VisitationDoneConfirmationOverlay(
    onConfirm: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) = CallConfirmationOverlay(
    question = Res.string.visitation_done_confirm_question,
    confirmText = Res.string.visitation_done_confirm_action,
    overlayTag = VISITATION_DONE_CONFIRM_TAG,
    confirmTag = VISITATION_DONE_CONFIRM_ACTION_TAG,
    closeTag = VISITATION_DONE_CONFIRM_CLOSE_TAG,
    onConfirm = onConfirm,
    onClose = onClose,
    modifier = modifier
)

/**
 * The shared "call this command?" dialog: a modal card over a press-swallowing scrim with a secondary close text
 * and a primary confirm button.
 */
@Composable
private fun CallConfirmationOverlay(
    question: StringResource,
    confirmText: StringResource,
    overlayTag: String,
    confirmTag: String,
    closeTag: String,
    onConfirm: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .background(BlackColor.copy(alpha = 0.6f))
            .pointerInput(Unit) { detectTapGestures { } }
            .testTag(overlayTag)
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
                text = stringResource(question),
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
                    text = stringResource(Res.string.mark_confirm_close),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .testTag(closeTag)
                        .clickable { onClose() }
                        .padding(Paddings.Small)
                )
                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.testTag(confirmTag)
                ) {
                    Text(text = stringResource(confirmText))
                }
            }
        }
    }
}

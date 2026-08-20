package se.kjellstrand.fieldshootingtimer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import se.kjellstrand.fieldshootingtimer.resources.Res
import se.kjellstrand.fieldshootingtimer.resources.add_tick
import se.kjellstrand.fieldshootingtimer.resources.competition
import se.kjellstrand.fieldshootingtimer.resources.graphic_eq
import se.kjellstrand.fieldshootingtimer.resources.play_arrow
import se.kjellstrand.fieldshootingtimer.resources.tutorial_done
import se.kjellstrand.fieldshootingtimer.resources.tutorial_next
import se.kjellstrand.fieldshootingtimer.resources.tutorial_skip
import se.kjellstrand.fieldshootingtimer.resources.tutorial_step_add_tick
import se.kjellstrand.fieldshootingtimer.resources.tutorial_step_drag_tick
import se.kjellstrand.fieldshootingtimer.resources.tutorial_step_mode
import se.kjellstrand.fieldshootingtimer.resources.tutorial_step_pinch
import se.kjellstrand.fieldshootingtimer.resources.tutorial_step_seek
import se.kjellstrand.fieldshootingtimer.resources.tutorial_step_signal
import se.kjellstrand.fieldshootingtimer.ui.theme.BlackColor
import se.kjellstrand.fieldshootingtimer.ui.theme.Paddings

internal const val TUTORIAL_OVERLAY_TAG = "TutorialOverlay"
internal const val TUTORIAL_NEXT_TAG = "TutorialNext"
internal const val TUTORIAL_SKIP_TAG = "TutorialSkip"

internal data class TutorialStep(
    val text: StringResource,
    val icon: DrawableResource? = null
)

/** The gestures and modes worth teaching, in the order a new user meets them. */
internal val tutorialSteps = listOf(
    TutorialStep(Res.string.tutorial_step_pinch),
    TutorialStep(Res.string.tutorial_step_add_tick, Res.drawable.add_tick),
    TutorialStep(Res.string.tutorial_step_drag_tick),
    TutorialStep(Res.string.tutorial_step_seek, Res.drawable.play_arrow),
    TutorialStep(Res.string.tutorial_step_signal, Res.drawable.graphic_eq),
    TutorialStep(Res.string.tutorial_step_mode, Res.drawable.competition)
)

/**
 * One tutorial step as a modal card over a dark scrim that swallows all
 * presses. Shown on first launch (persisted via the tutorialSeen setting)
 * and on demand from the radial menu's help item.
 */
@Composable
internal fun TutorialOverlay(
    stepIndex: Int,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val step = tutorialSteps[stepIndex]
    val isLast = stepIndex == tutorialSteps.lastIndex
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .background(BlackColor.copy(alpha = 0.6f))
            .pointerInput(Unit) { detectTapGestures { } }
            .testTag(TUTORIAL_OVERLAY_TAG)
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
                text = "${stepIndex + 1}/${tutorialSteps.size}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            step.icon?.let { icon ->
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .padding(top = Paddings.Medium)
                        .size(48.dp)
                )
            }
            Text(
                text = stringResource(step.text),
                style = MaterialTheme.typography.titleMedium,
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
                    text = stringResource(Res.string.tutorial_skip),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .testTag(TUTORIAL_SKIP_TAG)
                        .clickable { onSkip() }
                        .padding(Paddings.Small)
                )
                Spacer(modifier = Modifier.padding(Paddings.Small))
                Button(
                    onClick = onNext,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.testTag(TUTORIAL_NEXT_TAG)
                ) {
                    Text(
                        text = stringResource(
                            if (isLast) Res.string.tutorial_done else Res.string.tutorial_next
                        )
                    )
                }
            }
        }
    }
}

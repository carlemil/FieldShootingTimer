package se.kjellstrand.fieldshootingtimer.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import se.kjellstrand.fieldshootingtimer.domain.TimerMode
import se.kjellstrand.fieldshootingtimer.resources.Res
import se.kjellstrand.fieldshootingtimer.resources.add_tick
import se.kjellstrand.fieldshootingtimer.resources.cease_fire_beep
import se.kjellstrand.fieldshootingtimer.resources.cease_fire_voice
import se.kjellstrand.fieldshootingtimer.resources.competition
import se.kjellstrand.fieldshootingtimer.resources.dark_mode
import se.kjellstrand.fieldshootingtimer.resources.graphic_eq
import se.kjellstrand.fieldshootingtimer.resources.help
import se.kjellstrand.fieldshootingtimer.resources.light_mode
import se.kjellstrand.fieldshootingtimer.resources.mail
import se.kjellstrand.fieldshootingtimer.resources.menu
import se.kjellstrand.fieldshootingtimer.resources.mode_competition
import se.kjellstrand.fieldshootingtimer.resources.mode_training
import se.kjellstrand.fieldshootingtimer.resources.record_voice_over
import se.kjellstrand.fieldshootingtimer.resources.remove_tick
import se.kjellstrand.fieldshootingtimer.resources.send_feedback
import se.kjellstrand.fieldshootingtimer.resources.share
import se.kjellstrand.fieldshootingtimer.resources.share_app
import se.kjellstrand.fieldshootingtimer.resources.theme_dark
import se.kjellstrand.fieldshootingtimer.resources.theme_light
import se.kjellstrand.fieldshootingtimer.resources.training
import se.kjellstrand.fieldshootingtimer.ui.theme.Paddings
import kotlin.math.roundToInt

internal const val MENU_BUTTON_TAG = "RadialMenuButton"
internal const val MENU_ITEM_ADD_TICK_TAG = "RadialMenuItemAddTick"
internal const val MENU_ITEM_REMOVE_TICK_TAG = "RadialMenuItemRemoveTick"
internal const val MENU_ITEM_SHARE_TAG = "RadialMenuItemShare"
internal const val MENU_ITEM_MODE_TAG = "RadialMenuItemMode"
internal const val MENU_ITEM_TUTORIAL_TAG = "RadialMenuItemTutorial"
internal const val MENU_ITEM_THEME_TAG = "RadialMenuItemTheme"
internal const val MENU_ITEM_BEEP_TAG = "RadialMenuItemBeep"
internal const val MENU_ITEM_FEEDBACK_TAG = "RadialMenuItemFeedback"
internal const val MENU_SCRIM_TAG = "RadialMenuScrim"

private val MenuButtonSize = 38.dp

/** Gap between a row's button and its label. */
private val MenuItemSpacing = 10.dp

/** Vertical air between the rows. */
private val MenuRowSpacing = 18.dp

/** First row's top offset below the menu button. */
private val MenuTopOffset = 52.dp

/** Horizontal stride between the two landscape columns — wide enough for
 * the longest label ("Röstkommando vid eld upphör"). */
private val MenuColumnStride = 250.dp

/**
 * The app menu: a speed dial anchored at the top-start corner. The rows
 * slide down from beneath the menu button with a slightly underdamped
 * spring, each showing its round icon button with a text label to the
 * right. Portrait stacks all eight rows in one column; landscape
 * ([twoColumns]) splits them 4+4 since eight rows don't fit the height.
 *
 * Rows, top to bottom: add/remove tick (+/−, gated by [tickAdjustEnabled];
 * the menu stays open so several ticks can be added in a row), the
 * competition/training mode toggle ([modeToggleEnabled] gates it to when
 * the timer isn't running), the cease-fire beep toggle, the light/dark
 * theme toggle (toggles keep the menu open and show the active state),
 * then share, feedback, and help — which close the menu when tapped.
 *
 * [open] is hoisted so the caller can put a press-blocking scrim between
 * the rest of the app and this menu while it is open.
 */
@Composable
fun AppMenu(
    open: Boolean,
    onOpenChange: (Boolean) -> Unit,
    timerMode: TimerMode,
    modeToggleEnabled: Boolean,
    onToggleMode: () -> Unit,
    tickAdjustEnabled: Boolean,
    onAddTick: () -> Unit,
    onRemoveTick: () -> Unit,
    onShare: () -> Unit,
    onShowTutorial: () -> Unit,
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
    ceaseFireBeep: Boolean,
    onToggleCeaseFireBeep: () -> Unit,
    onSendFeedback: () -> Unit,
    twoColumns: Boolean,
    modifier: Modifier = Modifier
) {
    val progress by animateFloatAsState(
        targetValue = if (open) 1f else 0f,
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "appMenuSlideOut"
    )

    data class Entry(
        val icon: DrawableResource,
        val label: String,
        val tag: String,
        val onClick: () -> Unit
    )

    val entries = listOf(
        Entry(
            Res.drawable.add_tick, stringResource(Res.string.add_tick),
            MENU_ITEM_ADD_TICK_TAG
        ) { if (tickAdjustEnabled) onAddTick() },
        Entry(
            Res.drawable.remove_tick, stringResource(Res.string.remove_tick),
            MENU_ITEM_REMOVE_TICK_TAG
        ) { if (tickAdjustEnabled) onRemoveTick() },
        Entry(
            if (timerMode == TimerMode.Competition) Res.drawable.competition else Res.drawable.training,
            stringResource(
                if (timerMode == TimerMode.Competition) Res.string.mode_competition
                else Res.string.mode_training
            ),
            MENU_ITEM_MODE_TAG
        ) { if (modeToggleEnabled) onToggleMode() },
        Entry(
            if (ceaseFireBeep) Res.drawable.graphic_eq else Res.drawable.record_voice_over,
            stringResource(
                if (ceaseFireBeep) Res.string.cease_fire_beep else Res.string.cease_fire_voice
            ),
            MENU_ITEM_BEEP_TAG,
            onToggleCeaseFireBeep
        ),
        Entry(
            if (darkTheme) Res.drawable.dark_mode else Res.drawable.light_mode,
            stringResource(if (darkTheme) Res.string.theme_dark else Res.string.theme_light),
            MENU_ITEM_THEME_TAG,
            onToggleTheme
        ),
        Entry(
            Res.drawable.share, stringResource(Res.string.share_app),
            MENU_ITEM_SHARE_TAG
        ) {
            onOpenChange(false)
            onShare()
        },
        Entry(
            Res.drawable.mail, stringResource(Res.string.send_feedback),
            MENU_ITEM_FEEDBACK_TAG
        ) {
            onOpenChange(false)
            onSendFeedback()
        },
        Entry(
            Res.drawable.help, stringResource(Res.string.help),
            MENU_ITEM_TUTORIAL_TAG
        ) {
            onOpenChange(false)
            onShowTutorial()
        }
    )

    val density = LocalDensity.current
    val rowStridePx = with(density) { (MenuButtonSize + MenuRowSpacing).toPx() }
    val topOffsetPx = with(density) { MenuTopOffset.toPx() }
    val columnStridePx = with(density) { MenuColumnStride.toPx() }
    val rowsPerColumn = if (twoColumns) 4 else entries.size

    Box(modifier = modifier) {
        // Rows are composed before (= beneath) the menu button and leave the
        // composition entirely once the closing spring has settled.
        if (progress > 0.01f) {
            entries.forEachIndexed { index, entry ->
                val column = index / rowsPerColumn
                val row = index % rowsPerColumn
                val targetY = topOffsetPx + rowStridePx * row
                val targetX = columnStridePx * column
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                (targetX * progress).roundToInt(),
                                (targetY * progress).roundToInt()
                            )
                        }
                        // Saturate at half the travel: the underdamped spring
                        // overshoots around 1.0, and an alpha that oscillates
                        // across exactly 1.0 toggles the compositing layer on
                        // and off — visible as a flicker as the fan settles.
                        .graphicsLayer { alpha = (progress * 2f).coerceIn(0f, 1f) }
                ) {
                    IconButton(
                        onClick = entry.onClick,
                        modifier = Modifier
                            .size(MenuButtonSize)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                            .border(
                                Paddings.Tiny,
                                MaterialTheme.colorScheme.onBackground,
                                CircleShape
                            )
                            .testTag(entry.tag)
                    ) {
                        Icon(
                            painter = painterResource(entry.icon),
                            contentDescription = entry.label,
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(Modifier.width(MenuItemSpacing))
                    // Solid theme-following pill: light with dark text in
                    // light mode, inverted (dark with light text) in dark —
                    // outlined like the round buttons.
                    Text(
                        text = entry.label,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(
                                Paddings.Tiny,
                                MaterialTheme.colorScheme.onBackground,
                                RoundedCornerShape(6.dp)
                            )
                            .clickable { entry.onClick() }
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }
        IconButton(
            onClick = { onOpenChange(!open) },
            modifier = Modifier
                .size(MenuButtonSize)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .border(Paddings.Tiny, MaterialTheme.colorScheme.onBackground, CircleShape)
                .testTag(MENU_BUTTON_TAG)
        ) {
            Icon(
                painter = painterResource(Res.drawable.menu),
                contentDescription = stringResource(Res.string.menu),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

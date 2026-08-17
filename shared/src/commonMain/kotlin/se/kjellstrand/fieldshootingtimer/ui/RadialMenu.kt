package se.kjellstrand.fieldshootingtimer.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import se.kjellstrand.fieldshootingtimer.domain.TimerMode
import se.kjellstrand.fieldshootingtimer.resources.Res
import se.kjellstrand.fieldshootingtimer.resources.add_tick
import se.kjellstrand.fieldshootingtimer.resources.competition
import se.kjellstrand.fieldshootingtimer.resources.help
import se.kjellstrand.fieldshootingtimer.resources.menu
import se.kjellstrand.fieldshootingtimer.resources.mode_competition
import se.kjellstrand.fieldshootingtimer.resources.mode_training
import se.kjellstrand.fieldshootingtimer.resources.remove_tick
import se.kjellstrand.fieldshootingtimer.resources.share
import se.kjellstrand.fieldshootingtimer.resources.share_app
import se.kjellstrand.fieldshootingtimer.resources.training
import se.kjellstrand.fieldshootingtimer.ui.theme.BlackColor
import se.kjellstrand.fieldshootingtimer.ui.theme.WhiteColor
import kotlin.math.roundToInt

internal const val MENU_BUTTON_TAG = "RadialMenuButton"
internal const val MENU_ITEM_ADD_TICK_TAG = "RadialMenuItemAddTick"
internal const val MENU_ITEM_REMOVE_TICK_TAG = "RadialMenuItemRemoveTick"
internal const val MENU_ITEM_SHARE_TAG = "RadialMenuItemShare"
internal const val MENU_ITEM_MODE_TAG = "RadialMenuItemMode"
internal const val MENU_ITEM_TUTORIAL_TAG = "RadialMenuItemTutorial"
internal const val MENU_SCRIM_TAG = "RadialMenuScrim"

/**
 * Distance from the menu button's center to each fanned-out item's center.
 * With five items 20° apart, adjacent centers sit ~2·r·sin(10°) ≈ 61dp
 * apart — comfortably above the 48dp button size.
 */
private val MenuItemRadius = 175.dp

/**
 * A circular menu button whose items fan out on an arc when opened. The items
 * are composed behind the button, so at rest they hide beneath it; a slightly
 * underdamped spring floats them out to their arc positions with a small
 * elastic overshoot, and pulls them back in on close.
 *
 * Items: add/remove tick (+/−, gated by [tickAdjustEnabled]; the menu stays
 * open so several ticks can be added in a row), a competition/training mode
 * toggle whose icon shows the active mode ([modeToggleEnabled] gates it to
 * when the timer is idle), share, and help (reopens the tutorial).
 * [openTowardsStart] picks the arc direction so the items fan toward the
 * screen's interior from either top corner.
 *
 * [open] is hoisted so the caller can put a press-blocking scrim between the
 * rest of the app and this menu while it is open.
 */
@Composable
fun RadialMenu(
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
    openTowardsStart: Boolean,
    modifier: Modifier = Modifier
) {
    val progress by animateFloatAsState(
        targetValue = if (open) 1f else 0f,
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "radialMenuFanOut"
    )
    val radiusPx = with(LocalDensity.current) { MenuItemRadius.toPx() }
    // Degrees: 0 = right, 90 = straight down. Fan into the screen from the
    // anchoring corner.
    val itemAngles = if (openTowardsStart) {
        listOf(90f, 110f, 130f, 150f, 170f)
    } else {
        listOf(90f, 70f, 50f, 30f, 10f)
    }

    Box(modifier = modifier) {
        // Items are composed before (= beneath) the menu button and leave the
        // composition entirely once the closing spring has settled.
        if (progress > 0.01f) {
            RadialMenuItem(
                angleDeg = itemAngles[0],
                progress = progress,
                radiusPx = radiusPx,
                icon = Res.drawable.add_tick,
                contentDescription = stringResource(Res.string.add_tick),
                tag = MENU_ITEM_ADD_TICK_TAG,
                onClick = { if (tickAdjustEnabled) onAddTick() }
            )
            RadialMenuItem(
                angleDeg = itemAngles[1],
                progress = progress,
                radiusPx = radiusPx,
                icon = Res.drawable.remove_tick,
                contentDescription = stringResource(Res.string.remove_tick),
                tag = MENU_ITEM_REMOVE_TICK_TAG,
                onClick = { if (tickAdjustEnabled) onRemoveTick() }
            )
            RadialMenuItem(
                angleDeg = itemAngles[2],
                progress = progress,
                radiusPx = radiusPx,
                icon = if (timerMode == TimerMode.Competition) {
                    Res.drawable.competition
                } else {
                    Res.drawable.training
                },
                contentDescription = stringResource(
                    if (timerMode == TimerMode.Competition) {
                        Res.string.mode_competition
                    } else {
                        Res.string.mode_training
                    }
                ),
                tag = MENU_ITEM_MODE_TAG,
                onClick = { if (modeToggleEnabled) onToggleMode() }
            )
            RadialMenuItem(
                angleDeg = itemAngles[3],
                progress = progress,
                radiusPx = radiusPx,
                icon = Res.drawable.share,
                contentDescription = stringResource(Res.string.share_app),
                tag = MENU_ITEM_SHARE_TAG,
                onClick = {
                    onOpenChange(false)
                    onShare()
                }
            )
            RadialMenuItem(
                angleDeg = itemAngles[4],
                progress = progress,
                radiusPx = radiusPx,
                icon = Res.drawable.help,
                contentDescription = stringResource(Res.string.help),
                tag = MENU_ITEM_TUTORIAL_TAG,
                onClick = {
                    onOpenChange(false)
                    onShowTutorial()
                }
            )
        }
        IconButton(
            onClick = { onOpenChange(!open) },
            modifier = Modifier
                .clip(CircleShape)
                .background(WhiteColor)
                .testTag(MENU_BUTTON_TAG)
        ) {
            Icon(
                painter = painterResource(Res.drawable.menu),
                contentDescription = stringResource(Res.string.menu),
                tint = BlackColor
            )
        }
    }
}

@Composable
private fun RadialMenuItem(
    angleDeg: Float,
    progress: Float,
    radiusPx: Float,
    icon: DrawableResource,
    contentDescription: String,
    tag: String,
    onClick: () -> Unit
) {
    val center = polarToCartesian(Offset.Zero, radiusPx * progress, angleDeg)
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .offset { IntOffset(center.x.roundToInt(), center.y.roundToInt()) }
            .clip(CircleShape)
            .background(WhiteColor)
            .testTag(tag)
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = contentDescription,
            tint = BlackColor
        )
    }
}

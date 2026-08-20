package se.kjellstrand.fieldshootingtimer.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import se.kjellstrand.fieldshootingtimer.resources.cease_fire_beep
import se.kjellstrand.fieldshootingtimer.resources.cease_fire_voice
import se.kjellstrand.fieldshootingtimer.resources.competition
import se.kjellstrand.fieldshootingtimer.resources.dark_mode
import se.kjellstrand.fieldshootingtimer.resources.graphic_eq
import se.kjellstrand.fieldshootingtimer.resources.help
import se.kjellstrand.fieldshootingtimer.resources.light_mode
import se.kjellstrand.fieldshootingtimer.resources.menu
import se.kjellstrand.fieldshootingtimer.resources.mode_competition
import se.kjellstrand.fieldshootingtimer.resources.mode_training
import se.kjellstrand.fieldshootingtimer.resources.record_voice_over
import se.kjellstrand.fieldshootingtimer.resources.remove_tick
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
internal const val MENU_SCRIM_TAG = "RadialMenuScrim"

/**
 * The items fan out in two layers so nothing sits too far from the button:
 * the theme toggle, share, and help on the inner arc; the timer-editing
 * items (+/−, mode) and the beep toggle on the outer. The radii and angle sets
 * are chosen so every neighbor distance lands in a tight 68–73dp band:
 * outer 2·158·sin(13.3°) ≈ 73dp, inner 2·90·sin(22.5°) ≈ 69dp, ring gap
 * 158−90 = 68dp, and the staggered cross-ring pairs ≈ 70–71dp.
 */
private val InnerMenuItemRadius = 90.dp
private val OuterMenuItemRadius = 158.dp

/** 20% below the stock 48dp IconButton, keeping the fan compact. */
private val MenuButtonSize = 38.dp

/**
 * A circular menu button whose items fan out on an arc when opened. The items
 * are composed behind the button, so at rest they hide beneath it; a slightly
 * underdamped spring floats them out to their arc positions with a small
 * elastic overshoot, and pulls them back in on close.
 *
 * Two layers: the inner arc holds the light/dark theme toggle (icon shows
 * the active theme; the menu stays open so the switch is seen immediately),
 * share, and help (reopens the tutorial). The outer arc holds add/remove
 * tick (+/−, gated by [tickAdjustEnabled]; the menu stays open so several
 * ticks can be added in a row), a competition/training mode toggle whose
 * icon shows the active mode ([modeToggleEnabled] gates it to when the
 * timer isn't running), and the cease-fire beep toggle (icon shows the
 * active cue style; a short signal at the yellow segment's end instead of
 * the spoken "Eld upphör!").
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
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
    ceaseFireBeep: Boolean,
    onToggleCeaseFireBeep: () -> Unit,
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
    val innerRadiusPx = with(LocalDensity.current) { InnerMenuItemRadius.toPx() }
    val outerRadiusPx = with(LocalDensity.current) { OuterMenuItemRadius.toPx() }
    // Degrees: 0 = right, 90 = straight down. Fan into the screen from the
    // anchoring corner.
    val outerAngles = if (openTowardsStart) {
        listOf(90f, 117f, 143f, 170f)
    } else {
        listOf(90f, 63f, 37f, 10f)
    }
    val innerAngles = if (openTowardsStart) {
        listOf(90f, 135f, 180f)
    } else {
        listOf(90f, 45f, 0f)
    }

    Box(modifier = modifier) {
        // Items are composed before (= beneath) the menu button and leave the
        // composition entirely once the closing spring has settled.
        if (progress > 0.01f) {
            // Outer layer: timer editing + theme.
            RadialMenuItem(
                angleDeg = outerAngles[0],
                progress = progress,
                radiusPx = outerRadiusPx,
                icon = Res.drawable.add_tick,
                contentDescription = stringResource(Res.string.add_tick),
                tag = MENU_ITEM_ADD_TICK_TAG,
                onClick = { if (tickAdjustEnabled) onAddTick() }
            )
            RadialMenuItem(
                angleDeg = outerAngles[1],
                progress = progress,
                radiusPx = outerRadiusPx,
                icon = Res.drawable.remove_tick,
                contentDescription = stringResource(Res.string.remove_tick),
                tag = MENU_ITEM_REMOVE_TICK_TAG,
                onClick = { if (tickAdjustEnabled) onRemoveTick() }
            )
            RadialMenuItem(
                angleDeg = outerAngles[2],
                progress = progress,
                radiusPx = outerRadiusPx,
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
                angleDeg = outerAngles[3],
                progress = progress,
                radiusPx = outerRadiusPx,
                icon = if (ceaseFireBeep) {
                    Res.drawable.graphic_eq
                } else {
                    Res.drawable.record_voice_over
                },
                contentDescription = stringResource(
                    if (ceaseFireBeep) Res.string.cease_fire_beep
                    else Res.string.cease_fire_voice
                ),
                tag = MENU_ITEM_BEEP_TAG,
                onClick = onToggleCeaseFireBeep
            )
            // Inner layer: theme toggle + app-level items.
            RadialMenuItem(
                angleDeg = innerAngles[0],
                progress = progress,
                radiusPx = innerRadiusPx,
                icon = if (darkTheme) Res.drawable.dark_mode else Res.drawable.light_mode,
                contentDescription = stringResource(
                    if (darkTheme) Res.string.theme_dark else Res.string.theme_light
                ),
                tag = MENU_ITEM_THEME_TAG,
                onClick = onToggleTheme
            )
            RadialMenuItem(
                angleDeg = innerAngles[1],
                progress = progress,
                radiusPx = innerRadiusPx,
                icon = Res.drawable.share,
                contentDescription = stringResource(Res.string.share_app),
                tag = MENU_ITEM_SHARE_TAG,
                onClick = {
                    onOpenChange(false)
                    onShare()
                }
            )
            RadialMenuItem(
                angleDeg = innerAngles[2],
                progress = progress,
                radiusPx = innerRadiusPx,
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
            .size(MenuButtonSize)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .border(Paddings.Tiny, MaterialTheme.colorScheme.onBackground, CircleShape)
            .testTag(tag)
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}

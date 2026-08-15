package se.kjellstrand.fieldshootingtimer.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import se.kjellstrand.fieldshootingtimer.domain.TimerMode
import se.kjellstrand.fieldshootingtimer.ui.theme.FieldShootingTimerTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class RadialMenuTest {

    @Test
    fun `items are absent while the menu is closed`() = runComposeUiTest {
        setContent {
            FieldShootingTimerTheme(dynamicColor = false) {
                // Fixed-size canvas so the fanned-out items stay inside the
                // compose root and remain clickable.
                Box(Modifier.size(300.dp)) {
                    var open by remember { mutableStateOf(false) }
                    RadialMenu(
                    open = open,
                    onOpenChange = { open = it },
                    timerMode = TimerMode.Training,
                    modeToggleEnabled = true,
                    onToggleMode = {},
                    onShare = {},
                    onShowTutorial = {},
                    openTowardsStart = false
                    )
                }
            }
        }
        onNodeWithTag(MENU_BUTTON_TAG).assertExists()
        onNodeWithTag(MENU_ITEM_SHARE_TAG).assertDoesNotExist()
        onNodeWithTag(MENU_ITEM_MODE_TAG).assertDoesNotExist()
    }

    @Test
    fun `opening the menu fans out both items`() = runComposeUiTest {
        setContent {
            FieldShootingTimerTheme(dynamicColor = false) {
                // Fixed-size canvas so the fanned-out items stay inside the
                // compose root and remain clickable.
                Box(Modifier.size(300.dp)) {
                    var open by remember { mutableStateOf(false) }
                    RadialMenu(
                    open = open,
                    onOpenChange = { open = it },
                    timerMode = TimerMode.Training,
                    modeToggleEnabled = true,
                    onToggleMode = {},
                    onShare = {},
                    onShowTutorial = {},
                    openTowardsStart = false
                    )
                }
            }
        }
        onNodeWithTag(MENU_BUTTON_TAG).performClick()
        onNodeWithTag(MENU_ITEM_SHARE_TAG).assertExists()
        onNodeWithTag(MENU_ITEM_MODE_TAG).assertExists()
    }

    @Test
    fun `share item fires the callback and closes the menu`() = runComposeUiTest {
        var shared = 0
        setContent {
            FieldShootingTimerTheme(dynamicColor = false) {
                // Fixed-size canvas so the fanned-out items stay inside the
                // compose root and remain clickable.
                Box(Modifier.size(300.dp)) {
                    var open by remember { mutableStateOf(false) }
                    RadialMenu(
                    open = open,
                    onOpenChange = { open = it },
                    timerMode = TimerMode.Training,
                    modeToggleEnabled = true,
                    onToggleMode = {},
                    onShare = { shared++ },
                    onShowTutorial = {},
                    openTowardsStart = false
                    )
                }
            }
        }
        onNodeWithTag(MENU_BUTTON_TAG).performClick()
        onNodeWithTag(MENU_ITEM_SHARE_TAG).performClick()
        assertEquals(1, shared)
        onNodeWithTag(MENU_ITEM_SHARE_TAG).assertDoesNotExist()
    }

    @Test
    fun `mode item toggles the mode in the view model`() = runComposeUiTest {
        val vm = TimerViewModel()
        setContent {
            FieldShootingTimerTheme(dynamicColor = false) {
                // Fixed-size canvas so the fanned-out items stay inside the
                // compose root and remain clickable.
                Box(Modifier.size(300.dp)) {
                    var open by remember { mutableStateOf(false) }
                    RadialMenu(
                    open = open,
                    onOpenChange = { open = it },
                    timerMode = TimerMode.Training,
                    modeToggleEnabled = true,
                    onToggleMode = { vm.setTimerMode(TimerMode.Competition) },
                    onShare = {},
                    onShowTutorial = {},
                    openTowardsStart = false
                    )
                }
            }
        }
        onNodeWithTag(MENU_BUTTON_TAG).performClick()
        onNodeWithTag(MENU_ITEM_MODE_TAG).performClick()
        assertEquals(TimerMode.Competition, vm.uiStateFlow.value.timerMode)
    }

    @Test
    fun `tutorial item fires the callback and closes the menu`() = runComposeUiTest {
        var tutorials = 0
        setContent {
            FieldShootingTimerTheme(dynamicColor = false) {
                // Fixed-size canvas so the fanned-out items stay inside the
                // compose root and remain clickable.
                Box(Modifier.size(300.dp)) {
                    var open by remember { mutableStateOf(false) }
                    RadialMenu(
                    open = open,
                    onOpenChange = { open = it },
                    timerMode = TimerMode.Training,
                    modeToggleEnabled = true,
                    onToggleMode = {},
                    onShare = {},
                    onShowTutorial = { tutorials++ },
                    openTowardsStart = false
                    )
                }
            }
        }
        onNodeWithTag(MENU_BUTTON_TAG).performClick()
        onNodeWithTag(MENU_ITEM_TUTORIAL_TAG).performClick()
        assertEquals(1, tutorials)
        onNodeWithTag(MENU_ITEM_TUTORIAL_TAG).assertDoesNotExist()
    }

    @Test
    fun `mode item is inert when disabled`() = runComposeUiTest {
        var toggles = 0
        setContent {
            FieldShootingTimerTheme(dynamicColor = false) {
                // Fixed-size canvas so the fanned-out items stay inside the
                // compose root and remain clickable.
                Box(Modifier.size(300.dp)) {
                    var open by remember { mutableStateOf(false) }
                    RadialMenu(
                    open = open,
                    onOpenChange = { open = it },
                    timerMode = TimerMode.Training,
                    modeToggleEnabled = false,
                    onToggleMode = { toggles++ },
                    onShare = {},
                    onShowTutorial = {},
                    openTowardsStart = false
                    )
                }
            }
        }
        onNodeWithTag(MENU_BUTTON_TAG).performClick()
        onNodeWithTag(MENU_ITEM_MODE_TAG).performClick()
        assertTrue(toggles == 0)
    }
}

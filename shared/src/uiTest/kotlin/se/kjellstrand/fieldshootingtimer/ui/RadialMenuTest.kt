package se.kjellstrand.fieldshootingtimer.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ComposeUiTest
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

    // Fixed-size canvas so the fanned-out items stay inside the compose root
    // and remain clickable.
    private fun ComposeUiTest.setMenuContent(
        timerMode: TimerMode = TimerMode.Training,
        modeToggleEnabled: Boolean = true,
        onToggleMode: () -> Unit = {},
        tickAdjustEnabled: Boolean = true,
        onAddTick: () -> Unit = {},
        onRemoveTick: () -> Unit = {},
        onShare: () -> Unit = {},
        onShowTutorial: () -> Unit = {},
        darkTheme: Boolean = false,
        onToggleTheme: () -> Unit = {},
        ceaseFireBeep: Boolean = false,
        onToggleCeaseFireBeep: () -> Unit = {},
        onSendFeedback: () -> Unit = {}
    ) {
        setContent {
            FieldShootingTimerTheme(dynamicColor = false) {
                Box(Modifier.size(300.dp)) {
                    var open by remember { mutableStateOf(false) }
                    RadialMenu(
                        open = open,
                        onOpenChange = { open = it },
                        timerMode = timerMode,
                        modeToggleEnabled = modeToggleEnabled,
                        onToggleMode = onToggleMode,
                        tickAdjustEnabled = tickAdjustEnabled,
                        onAddTick = onAddTick,
                        onRemoveTick = onRemoveTick,
                        onShare = onShare,
                        onShowTutorial = onShowTutorial,
                        darkTheme = darkTheme,
                        onToggleTheme = onToggleTheme,
                        ceaseFireBeep = ceaseFireBeep,
                        onToggleCeaseFireBeep = onToggleCeaseFireBeep,
                        onSendFeedback = onSendFeedback,
                        openTowardsStart = false
                    )
                }
            }
        }
    }

    @Test
    fun `items are absent while the menu is closed`() = runComposeUiTest {
        setMenuContent()
        onNodeWithTag(MENU_BUTTON_TAG).assertExists()
        onNodeWithTag(MENU_ITEM_SHARE_TAG).assertDoesNotExist()
        onNodeWithTag(MENU_ITEM_MODE_TAG).assertDoesNotExist()
        onNodeWithTag(MENU_ITEM_ADD_TICK_TAG).assertDoesNotExist()
        onNodeWithTag(MENU_ITEM_REMOVE_TICK_TAG).assertDoesNotExist()
    }

    @Test
    fun `opening the menu fans out every item`() = runComposeUiTest {
        setMenuContent()
        onNodeWithTag(MENU_BUTTON_TAG).performClick()
        onNodeWithTag(MENU_ITEM_SHARE_TAG).assertExists()
        onNodeWithTag(MENU_ITEM_MODE_TAG).assertExists()
        onNodeWithTag(MENU_ITEM_ADD_TICK_TAG).assertExists()
        onNodeWithTag(MENU_ITEM_REMOVE_TICK_TAG).assertExists()
        onNodeWithTag(MENU_ITEM_TUTORIAL_TAG).assertExists()
        onNodeWithTag(MENU_ITEM_THEME_TAG).assertExists()
    }

    @Test
    fun `feedback item fires the callback and closes the menu`() = runComposeUiTest {
        var feedbacks = 0
        setMenuContent(onSendFeedback = { feedbacks++ })
        onNodeWithTag(MENU_BUTTON_TAG).performClick()
        onNodeWithTag(MENU_ITEM_FEEDBACK_TAG).performClick()
        assertEquals(1, feedbacks)
        onNodeWithTag(MENU_ITEM_FEEDBACK_TAG).assertDoesNotExist()
    }

    @Test
    fun `beep item fires the callback and keeps the menu open`() = runComposeUiTest {
        var toggles = 0
        setMenuContent(onToggleCeaseFireBeep = { toggles++ })
        onNodeWithTag(MENU_BUTTON_TAG).performClick()
        onNodeWithTag(MENU_ITEM_BEEP_TAG).performClick()
        assertEquals(1, toggles)
        onNodeWithTag(MENU_ITEM_BEEP_TAG).assertExists()
    }

    @Test
    fun `theme item fires the callback and keeps the menu open`() = runComposeUiTest {
        var toggles = 0
        setMenuContent(onToggleTheme = { toggles++ })
        onNodeWithTag(MENU_BUTTON_TAG).performClick()
        onNodeWithTag(MENU_ITEM_THEME_TAG).performClick()
        assertEquals(1, toggles)
        // Stays open so the theme switch is seen immediately.
        onNodeWithTag(MENU_ITEM_THEME_TAG).assertExists()
    }

    @Test
    fun `share item fires the callback and closes the menu`() = runComposeUiTest {
        var shared = 0
        setMenuContent(onShare = { shared++ })
        onNodeWithTag(MENU_BUTTON_TAG).performClick()
        onNodeWithTag(MENU_ITEM_SHARE_TAG).performClick()
        assertEquals(1, shared)
        onNodeWithTag(MENU_ITEM_SHARE_TAG).assertDoesNotExist()
    }

    @Test
    fun `mode item toggles the mode in the view model`() = runComposeUiTest {
        val vm = TimerViewModel()
        setMenuContent(onToggleMode = { vm.setTimerMode(TimerMode.Competition) })
        onNodeWithTag(MENU_BUTTON_TAG).performClick()
        onNodeWithTag(MENU_ITEM_MODE_TAG).performClick()
        assertEquals(TimerMode.Competition, vm.uiStateFlow.value.timerMode)
    }

    @Test
    fun `tutorial item fires the callback and closes the menu`() = runComposeUiTest {
        var tutorials = 0
        setMenuContent(onShowTutorial = { tutorials++ })
        onNodeWithTag(MENU_BUTTON_TAG).performClick()
        onNodeWithTag(MENU_ITEM_TUTORIAL_TAG).performClick()
        assertEquals(1, tutorials)
        onNodeWithTag(MENU_ITEM_TUTORIAL_TAG).assertDoesNotExist()
    }

    @Test
    fun `mode item is inert when disabled`() = runComposeUiTest {
        var toggles = 0
        setMenuContent(modeToggleEnabled = false, onToggleMode = { toggles++ })
        onNodeWithTag(MENU_BUTTON_TAG).performClick()
        onNodeWithTag(MENU_ITEM_MODE_TAG).performClick()
        assertTrue(toggles == 0)
    }

    @Test
    fun `add tick item adds a thumb and keeps the menu open`() = runComposeUiTest {
        val vm = TimerViewModel()
        setMenuContent(onAddTick = { vm.addNewThumbValue(vm.rangeFlow.value) })
        onNodeWithTag(MENU_BUTTON_TAG).performClick()
        onNodeWithTag(MENU_ITEM_ADD_TICK_TAG).performClick()
        assertEquals(1, vm.uiStateFlow.value.thumbValues.size)
        assertTrue(vm.uiStateFlow.value.thumbValues.first() in 11f..17f)
        // Stays open so several ticks can be added in a row.
        onNodeWithTag(MENU_ITEM_ADD_TICK_TAG).assertExists()
    }

    @Test
    fun `remove tick item drops the last thumb`() = runComposeUiTest {
        val vm = TimerViewModel()
        vm.setThumbValues(listOf(12f, 14f))
        setMenuContent(onRemoveTick = vm::dropLastThumbValue)
        onNodeWithTag(MENU_BUTTON_TAG).performClick()
        onNodeWithTag(MENU_ITEM_REMOVE_TICK_TAG).performClick()
        assertEquals(listOf(12f), vm.uiStateFlow.value.thumbValues)
    }

    @Test
    fun `tick items are inert when disabled`() = runComposeUiTest {
        var adds = 0
        var removes = 0
        setMenuContent(
            tickAdjustEnabled = false,
            onAddTick = { adds++ },
            onRemoveTick = { removes++ }
        )
        onNodeWithTag(MENU_BUTTON_TAG).performClick()
        onNodeWithTag(MENU_ITEM_ADD_TICK_TAG).performClick()
        onNodeWithTag(MENU_ITEM_REMOVE_TICK_TAG).performClick()
        assertEquals(0, adds)
        assertEquals(0, removes)
    }
}

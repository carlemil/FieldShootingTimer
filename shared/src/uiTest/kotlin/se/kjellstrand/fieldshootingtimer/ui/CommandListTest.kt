package se.kjellstrand.fieldshootingtimer.ui

import se.kjellstrand.fieldshootingtimer.domain.Command

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.isSelected
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import se.kjellstrand.fieldshootingtimer.ui.theme.FieldShootingTimerTheme
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class CommandListTest {

    @Test
    fun `highlights exactly one item`() = runComposeUiTest {
        setContent {
            FieldShootingTimerTheme(dynamicColor = false) {
                CommandList(commands = Command.listedCommands, highlighted = Command.Fire)
            }
        }
        onAllNodes(isSelected()).assertCountEquals(1)
    }

    @Test
    fun `highlighted row matches the given command`() = runComposeUiTest {
        setContent {
            FieldShootingTimerTheme(dynamicColor = false) {
                CommandList(commands = Command.listedCommands, highlighted = Command.Fire)
            }
        }
        onNodeWithTag("$COMMAND_LIST_ROW_TAG${Command.Fire.name}").assertIsSelected()
    }

    @Test
    fun `tapping a row reports its command`() = runComposeUiTest {
        var clicked: Command? = null
        setContent {
            FieldShootingTimerTheme(dynamicColor = false) {
                CommandList(
                    commands = Command.listedCommands,
                    highlighted = null,
                    onCommandClick = { clicked = it }
                )
            }
        }
        onNodeWithTag("$COMMAND_LIST_ROW_TAG${Command.CeaseFire.name}").performClick()
        assertEquals(Command.CeaseFire, clicked)
    }

    @Test
    fun `cease fire row reads BEEP when the beep setting is on`() = runComposeUiTest {
        setContent {
            FieldShootingTimerTheme(dynamicColor = false) {
                CommandList(
                    commands = Command.listedCommands,
                    highlighted = null,
                    ceaseFireBeep = true
                )
            }
        }
        onNodeWithText("BEEP!").assertExists()
        onNodeWithText("ELD UPPHÖR!").assertDoesNotExist()
    }

    @Test
    fun `cease fire row keeps its voice label when the beep setting is off`() = runComposeUiTest {
        setContent {
            FieldShootingTimerTheme(dynamicColor = false) {
                CommandList(commands = Command.listedCommands, highlighted = null)
            }
        }
        onNodeWithText("ELD UPPHÖR!").assertExists()
        onNodeWithText("BEEP!").assertDoesNotExist()
    }

    @Test
    fun `only the given commands are shown`() = runComposeUiTest {
        val withoutPreparation = Command.listedCommands - Command.Load - Command.AllReady
        setContent {
            FieldShootingTimerTheme(dynamicColor = false) {
                CommandList(commands = withoutPreparation, highlighted = Command.Fire)
            }
        }
        onNodeWithTag("$COMMAND_LIST_ROW_TAG${Command.Load.name}").assertDoesNotExist()
        onNodeWithTag("$COMMAND_LIST_ROW_TAG${Command.AllReady.name}").assertDoesNotExist()
        onNodeWithTag("$COMMAND_LIST_ROW_TAG${Command.Mark.name}").assertExists()
    }
}

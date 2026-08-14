package se.kjellstrand.fieldshootingtimer.ui

import se.kjellstrand.fieldshootingtimer.domain.Command

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.isSelected
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import se.kjellstrand.fieldshootingtimer.ui.theme.FieldShootingTimerTheme
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class CommandListTest {

    @Test
    fun `highlights exactly one item`() = runComposeUiTest {
        setContent {
            FieldShootingTimerTheme(dynamicColor = false) {
                CommandList(hlIndex = Command.entries.indexOf(Command.Fire))
            }
        }
        onAllNodes(isSelected()).assertCountEquals(1)
    }

    @Test
    fun `highlighted row matches the given index`() = runComposeUiTest {
        val fireIndex = Command.entries.indexOf(Command.Fire)
        setContent {
            FieldShootingTimerTheme(dynamicColor = false) {
                CommandList(hlIndex = fireIndex)
            }
        }
        onNodeWithTag("$COMMAND_LIST_ROW_TAG$fireIndex").assertIsSelected()
    }
}

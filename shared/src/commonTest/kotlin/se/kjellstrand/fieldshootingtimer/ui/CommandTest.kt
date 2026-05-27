package se.kjellstrand.fieldshootingtimer.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CommandTest {

    @Test
    fun `timedCommands contains the expected ordered sequence`() {
        assertEquals(
            listOf(
                Command.TenSecondsLeft,
                Command.Ready,
                Command.Fire,
                Command.CeaseFire,
                Command.UnloadWeapon,
                Command.Visitation
            ),
            Command.timedCommands
        )
    }

    @Test
    fun `every timedCommand has non-negative duration`() {
        assertTrue(Command.timedCommands.isNotEmpty(), "timedCommands must not be empty")
        Command.timedCommands.forEach { cmd ->
            assertTrue(cmd.duration >= 0, "${cmd.name} must have non-negative duration")
        }
    }

    @Test
    fun `audibleCommands contains entries that have an audio resource`() {
        assertEquals(
            listOf(
                Command.TenSecondsLeft,
                Command.Ready,
                Command.Fire,
                Command.CeaseFire,
                Command.UnloadWeapon,
                Command.Visitation
            ),
            Command.audibleCommands
        )
    }

    @Test
    fun `every audibleCommand has a non-null audio path`() {
        assertTrue(Command.audibleCommands.isNotEmpty(), "audibleCommands must not be empty")
        Command.audibleCommands.forEach { cmd ->
            assertNotNull(cmd.audioPath, "${cmd.name} must have a non-null audioPath")
        }
    }

    @Test
    fun `Command enum has the full set of 9 entries`() {
        assertEquals(9, Command.entries.size)
    }

    @Test
    fun `display-only commands carry -1 for duration and null audioPath`() {
        listOf(Command.Load, Command.AllReady, Command.Mark).forEach { cmd ->
            assertEquals(-1, cmd.duration, "${cmd.name} duration")
            assertNull(cmd.audioPath, "${cmd.name} audioPath")
        }
    }
}

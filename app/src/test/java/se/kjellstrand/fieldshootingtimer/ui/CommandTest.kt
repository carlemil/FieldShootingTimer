package se.kjellstrand.fieldshootingtimer.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CommandTest {

    // --- Fixed behavior: Command should expose curated lists instead of requiring callers to filter ---

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
        assertTrue("timedCommands must not be empty", Command.timedCommands.isNotEmpty())
        Command.timedCommands.forEach { cmd ->
            assertTrue("${cmd.name} must have non-negative duration", cmd.duration >= 0)
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
        assertTrue("audibleCommands must not be empty", Command.audibleCommands.isNotEmpty())
        Command.audibleCommands.forEach { cmd ->
            assertTrue("${cmd.name} must have a non-null audioPath", cmd.audioPath != null)
        }
    }

    // --- Guard tests: enum shape should not regress ---

    @Test
    fun `Command enum has the full set of 9 entries`() {
        assertEquals(9, Command.entries.size)
    }

    @Test
    fun `display-only commands carry -1 for duration and null audioPath`() {
        listOf(Command.Load, Command.AllReady, Command.Mark).forEach { cmd ->
            assertEquals("${cmd.name} duration", -1, cmd.duration)
            assertEquals("${cmd.name} audioPath", null, cmd.audioPath)
        }
    }
}

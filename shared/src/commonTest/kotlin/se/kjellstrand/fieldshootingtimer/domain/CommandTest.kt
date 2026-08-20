package se.kjellstrand.fieldshootingtimer.domain

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
                Command.UnloadWeaponDelay,
                Command.UnloadWeapon,
                Command.VisitationDelay,
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
                Command.Load,
                Command.AllReady,
                Command.TenSecondsLeft,
                Command.Ready,
                Command.Fire,
                Command.CeaseFire,
                Command.UnloadWeapon,
                Command.Visitation,
                Command.VisitationDone,
                Command.Mark
            ),
            Command.audibleCommands
        )
    }

    @Test
    fun `silent pacing delays are timed but neither listed nor audible`() {
        listOf(Command.UnloadWeaponDelay, Command.VisitationDelay).forEach { cmd ->
            assertTrue(cmd.duration >= 0, "${cmd.name} must be timed")
            assertTrue(!cmd.listed, "${cmd.name} must not get a list row")
            assertNull(cmd.audioPath, "${cmd.name} must be silent")
        }
        assertEquals(3, Command.UnloadWeaponDelay.duration)
        assertEquals(2, Command.VisitationDelay.duration)
    }

    @Test
    fun `listedCommands is entries without the pacing delays`() {
        assertEquals(
            Command.entries - Command.UnloadWeaponDelay - Command.VisitationDelay,
            Command.listedCommands
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
    fun `dialCommands is the timedCommands prefix through CeaseFire`() {
        assertEquals(
            listOf(
                Command.TenSecondsLeft,
                Command.Ready,
                Command.Fire,
                Command.CeaseFire
            ),
            Command.dialCommands
        )
        // ShootTimer slices dial durations with take(dialCommands.size), which
        // is only correct while dialCommands is a prefix of timedCommands.
        assertEquals(
            Command.timedCommands.take(Command.dialCommands.size),
            Command.dialCommands
        )
    }

    @Test
    fun `Command enum has the full set of 12 entries`() {
        assertEquals(12, Command.entries.size)
    }

    @Test
    fun `untimed commands carry -1 for duration but all have audio`() {
        // Load/AllReady are called during the competition countdown;
        // VisitationDone and Mark through their dialogs.
        listOf(
            Command.Load, Command.AllReady, Command.VisitationDone, Command.Mark
        ).forEach { cmd ->
            assertEquals(-1, cmd.duration, "${cmd.name} duration")
            assertNotNull(cmd.audioPath, "${cmd.name} audioPath")
        }
    }
}

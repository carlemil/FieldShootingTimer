package se.kjellstrand.fieldshootingtimer.ui

import se.kjellstrand.fieldshootingtimer.domain.Command

import kotlin.test.Test
import kotlin.test.assertEquals

class CommandHighlightTest {

    private val segments = listOf(7f, 3f, 5f, 3f, 4f, 2f) // boundaries 7,10,15,18,22,24

    private fun entriesIndexOf(command: Command) = Command.entries.indexOf(command)

    @Test
    fun `time zero highlights the first timed command`() {
        assertEquals(entriesIndexOf(Command.TenSecondsLeft), calculateHighlightedIndex(0f, segments))
    }

    @Test
    fun `each segment boundary advances the highlight`() {
        assertEquals(entriesIndexOf(Command.TenSecondsLeft), calculateHighlightedIndex(6.9f, segments))
        assertEquals(entriesIndexOf(Command.Ready), calculateHighlightedIndex(7f, segments))
        assertEquals(entriesIndexOf(Command.Fire), calculateHighlightedIndex(10f, segments))
        assertEquals(entriesIndexOf(Command.CeaseFire), calculateHighlightedIndex(15f, segments))
        assertEquals(entriesIndexOf(Command.UnloadWeapon), calculateHighlightedIndex(18f, segments))
        assertEquals(entriesIndexOf(Command.Visitation), calculateHighlightedIndex(22f, segments))
    }

    @Test
    fun `past the end the last timed command stays highlighted`() {
        assertEquals(entriesIndexOf(Command.Visitation), calculateHighlightedIndex(24f, segments))
        assertEquals(entriesIndexOf(Command.Visitation), calculateHighlightedIndex(999f, segments))
    }
}

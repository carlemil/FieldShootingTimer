package se.kjellstrand.fieldshootingtimer.ui

import se.kjellstrand.fieldshootingtimer.domain.Command
import se.kjellstrand.fieldshootingtimer.domain.TimerMode

import kotlin.test.Test
import kotlin.test.assertEquals

class CommandHighlightTest {

    private val segments = listOf(7f, 3f, 5f, 3f, 4f, 2f) // boundaries 7,10,15,18,22,24

    private fun training(time: Float, state: TimerRunningState = TimerRunningState.Running) =
        highlightedCommand(TimerMode.Training, state, time, segments)

    private fun competition(time: Float, state: TimerRunningState = TimerRunningState.Running) =
        highlightedCommand(TimerMode.Competition, state, time, segments)

    @Test
    fun `time zero highlights the first timed command`() {
        assertEquals(Command.TenSecondsLeft, training(0f))
    }

    @Test
    fun `each segment boundary advances the highlight`() {
        assertEquals(Command.TenSecondsLeft, training(6.9f))
        assertEquals(Command.Ready, training(7f))
        assertEquals(Command.Fire, training(10f))
        assertEquals(Command.CeaseFire, training(15f))
        assertEquals(Command.UnloadWeapon, training(18f))
        assertEquals(Command.Visitation, training(22f))
    }

    @Test
    fun `past the end the last timed command stays highlighted`() {
        assertEquals(Command.Visitation, training(24f))
        assertEquals(Command.Visitation, training(999f))
    }

    @Test
    fun `competition highlights Load before the start`() {
        assertEquals(Command.Load, competition(0f, TimerRunningState.NotStarted))
    }

    @Test
    fun `competition highlights Load through most of the countdown`() {
        assertEquals(Command.Load, competition(-60f))
        assertEquals(Command.Load, competition(-10.1f))
    }

    @Test
    fun `competition hands over to AllReady for the final ten countdown seconds`() {
        assertEquals(Command.AllReady, competition(-10f))
        assertEquals(Command.AllReady, competition(-0.1f))
    }

    @Test
    fun `competition follows the segments once the countdown ends`() {
        assertEquals(Command.TenSecondsLeft, competition(0f))
        assertEquals(Command.Fire, competition(10f))
    }

    @Test
    fun `training never highlights the preparation commands`() {
        assertEquals(Command.TenSecondsLeft, training(0f, TimerRunningState.NotStarted))
    }
}

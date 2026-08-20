package se.kjellstrand.fieldshootingtimer.ui

import se.kjellstrand.fieldshootingtimer.domain.Command
import se.kjellstrand.fieldshootingtimer.domain.TimerMode

import kotlin.test.Test
import kotlin.test.assertEquals

class CommandHighlightTest {

    // Training boundaries: 7, 10, 15, 18 (delay), 21, 25.
    private val trainingSegments = listOf(7f, 3f, 5f, 3f, 3f, 4f)

    // Competition boundaries: 7, 10, 15, 18 (delay), 21, 25 (delay), 27, 29.
    private val competitionSegments = listOf(7f, 3f, 5f, 3f, 3f, 4f, 2f, 2f)

    private fun training(time: Float, state: TimerRunningState = TimerRunningState.Running) =
        highlightedCommand(TimerMode.Training, state, time, trainingSegments)

    private fun competition(time: Float, state: TimerRunningState = TimerRunningState.Running) =
        highlightedCommand(TimerMode.Competition, state, time, competitionSegments)

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
        assertEquals(Command.UnloadWeapon, training(21f))
        // The Visitation stretch exists in competition only.
        assertEquals(Command.Visitation, competition(27f))
    }

    @Test
    fun `silent pacing delays keep the previous command highlighted`() {
        // UnloadWeaponDelay runs 18..21: the cease-fire row stays lit.
        assertEquals(Command.CeaseFire, training(18f))
        assertEquals(Command.CeaseFire, training(20.9f))
        // VisitationDelay runs 25..27 (competition): the unload row stays lit.
        assertEquals(Command.UnloadWeapon, competition(25f))
        assertEquals(Command.UnloadWeapon, competition(26.9f))
    }

    @Test
    fun `past the end the last timed command stays highlighted`() {
        assertEquals(Command.UnloadWeapon, training(25f))
        assertEquals(Command.UnloadWeapon, training(999f))
        assertEquals(Command.Visitation, competition(29f))
        assertEquals(Command.Visitation, competition(999f))
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

    @Test
    fun `competition parked mid-sequence follows the parked time instead of Load`() {
        assertEquals(Command.Fire, competition(10f, TimerRunningState.NotStarted))
    }

    @Test
    fun `competition parked in the countdown highlights AllReady`() {
        assertEquals(Command.AllReady, competition(-10f, TimerRunningState.NotStarted))
    }

    @Test
    fun `awaiting ready confirmation keeps AllReady highlighted at zero`() {
        assertEquals(
            Command.AllReady,
            highlightedCommand(
                TimerMode.Competition, TimerRunningState.Stopped, 0f, competitionSegments,
                awaitingReadyConfirmation = true
            )
        )
    }
}

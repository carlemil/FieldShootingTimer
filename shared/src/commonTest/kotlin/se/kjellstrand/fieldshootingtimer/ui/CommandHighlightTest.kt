package se.kjellstrand.fieldshootingtimer.ui

import se.kjellstrand.fieldshootingtimer.domain.Command
import se.kjellstrand.fieldshootingtimer.domain.TimerMode

import kotlin.test.Test
import kotlin.test.assertEquals

class CommandHighlightTest {

    // Training boundaries: 7, 10, 15, 18, 22.
    private val trainingSegments = listOf(7f, 3f, 5f, 3f, 4f)

    // Competition boundaries: 7, 10, 15, 18, 22 (delay), 24, 26.
    private val competitionSegments = listOf(7f, 3f, 5f, 3f, 4f, 2f, 2f)

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
        assertEquals(Command.UnloadWeapon, training(18f))
        // The Visitation stretch exists in competition only.
        assertEquals(Command.Visitation, competition(24f))
    }

    @Test
    fun `the silent pacing delay keeps the previous command highlighted`() {
        // VisitationDelay runs 22..24 (competition): the unload row stays lit.
        assertEquals(Command.UnloadWeapon, competition(22f))
        assertEquals(Command.UnloadWeapon, competition(23.9f))
    }

    @Test
    fun `past the end the last timed command stays highlighted`() {
        assertEquals(Command.UnloadWeapon, training(25f))
        assertEquals(Command.UnloadWeapon, training(999f))
        assertEquals(Command.Visitation, competition(29f))
        assertEquals(Command.Visitation, competition(999f))
    }

    @Test
    fun `a finished competition timer highlights Mark`() {
        // Tapping "MARKERA!" parks the timer Finished at the sequence end —
        // and a run that completes naturally lands in the same phase.
        assertEquals(Command.Mark, competition(29f, TimerRunningState.Finished))
        // Training has no Mark row: the last shown command keeps the highlight.
        assertEquals(Command.UnloadWeapon, training(25f, TimerRunningState.Finished))
    }

    @Test
    fun `competition highlights Load before the start`() {
        assertEquals(Command.Load, competition(0f, TimerRunningState.NotStarted))
    }

    @Test
    fun `competition parked at zero by seek highlights the first timed command`() {
        // Tapping "10 sekunder kvar!" parks at 0 — that must not read as
        // "before the start" the way an untouched timer does.
        assertEquals(
            Command.TenSecondsLeft,
            highlightedCommand(
                TimerMode.Competition, TimerRunningState.NotStarted, 0f,
                competitionSegments, parkedBySeek = true
            )
        )
    }

    @Test
    fun `competition highlights AllReady through the gap before the sequence`() {
        assertEquals(Command.AllReady, competition(-3f))
        assertEquals(Command.AllReady, competition(-0.1f))
    }

    @Test
    fun `competition follows the segments once the gap ends`() {
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
    fun `awaiting ready confirmation highlights AllReady at zero`() {
        assertEquals(
            Command.AllReady,
            highlightedCommand(
                TimerMode.Competition, TimerRunningState.NotStarted, 0f, competitionSegments,
                awaitingReadyConfirmation = true
            )
        )
    }
}

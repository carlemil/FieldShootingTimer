package se.kjellstrand.fieldshootingtimer.ui

import se.kjellstrand.fieldshootingtimer.domain.Command
import se.kjellstrand.fieldshootingtimer.domain.TimerMode

import kotlin.test.Test
import kotlin.test.assertEquals

class CommandHighlightTest {

    // Boundaries: 7, 10, 15, 18 — the same in both modes.
    private val trainingSegments = listOf(7f, 3f, 5f, 3f)
    private val competitionSegments = trainingSegments

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
    }

    @Test
    fun `past the end the last timed command stays highlighted`() {
        assertEquals(Command.CeaseFire, training(18f))
        assertEquals(Command.CeaseFire, training(999f))
        assertEquals(Command.CeaseFire, competition(999f))
    }

    @Test
    fun `a finished timer highlights the last call of its mode's closing chain`() {
        // Tapping "MARKERA!" parks the timer Finished at the sequence end —
        // and a run that completes naturally lands in the same phase.
        assertEquals(Command.Mark, competition(18f, TimerRunningState.Finished))
        // Training's chain ends with "Patron ur!".
        assertEquals(Command.UnloadWeapon, training(18f, TimerRunningState.Finished))
    }

    @Test
    fun `an open closing dialog highlights its own row`() {
        val finished = TimerRunningState.Finished
        assertEquals(
            Command.UnloadWeapon,
            highlightedCommand(
                TimerMode.Training, finished, 18f, trainingSegments,
                awaitingUnloadConfirmation = true
            )
        )
        assertEquals(
            Command.Visitation,
            highlightedCommand(
                TimerMode.Competition, finished, 18f, competitionSegments,
                awaitingVisitationConfirmation = true
            )
        )
        assertEquals(
            Command.VisitationDone,
            highlightedCommand(
                TimerMode.Competition, finished, 18f, competitionSegments,
                awaitingVisitationDoneConfirmation = true
            )
        )
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
    fun `awaiting the 10 sekunder kvar confirmation highlights its row at zero`() {
        assertEquals(
            Command.TenSecondsLeft,
            highlightedCommand(
                TimerMode.Competition, TimerRunningState.NotStarted, 0f, competitionSegments,
                awaitingTenSecondsConfirmation = true
            )
        )
    }

    @Test
    fun `competition follows the segments once running`() {
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

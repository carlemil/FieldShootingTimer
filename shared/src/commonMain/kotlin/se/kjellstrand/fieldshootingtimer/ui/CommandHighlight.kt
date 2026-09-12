package se.kjellstrand.fieldshootingtimer.ui

import se.kjellstrand.fieldshootingtimer.domain.Command
import se.kjellstrand.fieldshootingtimer.domain.TimerMode

/**
 * The command to highlight in the command list.
 *
 * An open dialog owns its row. Competition mode owns the pre-sequence
 * phase: "Ladda!" before the start. From 0 onward — and always in training
 * mode — the highlight follows the running segment, mapped back through
 * [Command.timedCommands] so reordering the enum can't silently shift it.
 * A finished timer sits on the last call of its mode's closing chain.
 */
internal fun highlightedCommand(
    mode: TimerMode,
    runningState: TimerRunningState,
    currentTime: Float,
    segmentDurations: List<Float>,
    awaitingReadyConfirmation: Boolean = false,
    awaitingTenSecondsConfirmation: Boolean = false,
    parkedBySeek: Boolean = false,
    awaitingUnloadConfirmation: Boolean = false,
    awaitingVisitationConfirmation: Boolean = false,
    awaitingVisitationDoneConfirmation: Boolean = false
): Command {
    if (awaitingReadyConfirmation) return Command.AllReady
    if (awaitingTenSecondsConfirmation) return Command.TenSecondsLeft
    if (awaitingUnloadConfirmation) return Command.UnloadWeapon
    if (awaitingVisitationConfirmation) return Command.Visitation
    if (awaitingVisitationDoneConfirmation) return Command.VisitationDone
    if (runningState == TimerRunningState.Finished) {
        return if (mode == TimerMode.Competition) Command.Mark else Command.UnloadWeapon
    }
    if (mode == TimerMode.Competition) {
        // Only an untouched timer (still at 0) reads as "before the start" —
        // a timer parked at 0 by tapping "10 sekunder kvar!" (or scrubbing
        // the hand there) follows its parked time like any other seek.
        if (runningState == TimerRunningState.NotStarted && currentTime == 0f && !parkedBySeek) {
            return Command.Load
        }
    }
    var accumulatedTime = 0f
    segmentDurations.forEachIndexed { index, duration ->
        accumulatedTime += duration
        if (currentTime < accumulatedTime) return Command.timedCommands[index]
    }
    return Command.timedCommands.last()
}

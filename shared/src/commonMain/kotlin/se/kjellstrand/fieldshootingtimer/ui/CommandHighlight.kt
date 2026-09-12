package se.kjellstrand.fieldshootingtimer.ui

import se.kjellstrand.fieldshootingtimer.domain.Command
import se.kjellstrand.fieldshootingtimer.domain.TimerMode
import se.kjellstrand.fieldshootingtimer.domain.timedCommandsFor

/**
 * The command to highlight in the command list.
 *
 * Competition mode owns the pre-sequence phase: "Ladda!" before the start
 * and behind its dialog, "Alla klara!" behind its dialog and through the
 * silent gap (negative [currentTime]) before the sequence. From 0 onward — and always in training mode — the highlight follows the
 * running segment, mapped back through [Command.timedCommands] so reordering
 * the enum can't silently shift it. Past the last boundary the final timed
 * command stays lit.
 */
internal fun highlightedCommand(
    mode: TimerMode,
    runningState: TimerRunningState,
    currentTime: Float,
    segmentDurations: List<Float>,
    awaitingReadyConfirmation: Boolean = false,
    parkedBySeek: Boolean = false,
    awaitingVisitationDoneConfirmation: Boolean = false
): Command {
    // Behind the "Alla klara?" dialog — the AllReady phase.
    if (awaitingReadyConfirmation) return Command.AllReady
    // The "Visitation klar?" dialog owns its row while it is open.
    if (awaitingVisitationDoneConfirmation) return Command.VisitationDone
    // A finished timer has otherwise reached the Mark phase; competition is
    // the mode that shows the row (training's list ends at UnloadWeapon,
    // which the segment walk below keeps highlighted there).
    if (mode == TimerMode.Competition && runningState == TimerRunningState.Finished) {
        return Command.Mark
    }
    if (mode == TimerMode.Competition) {
        // Only an untouched timer (still at 0) reads as "before the start" —
        // a timer parked at 0 by tapping "10 sekunder kvar!" (or scrubbing
        // the hand there) follows its parked time like any other seek.
        if (runningState == TimerRunningState.NotStarted && currentTime == 0f && !parkedBySeek) {
            return Command.Load
        }
        // The silent gap after the confirmed "Alla klara!" call.
        if (currentTime < 0f) return Command.AllReady
    }
    val timedCommands = timedCommandsFor(mode)
    var accumulatedTime = 0f
    segmentDurations.forEachIndexed { index, duration ->
        accumulatedTime += duration
        if (currentTime < accumulatedTime) {
            return listedCommandAtOrBefore(timedCommands, index)
        }
    }
    return listedCommandAtOrBefore(timedCommands, timedCommands.lastIndex)
}

/**
 * The command list has no rows for the silent pacing delays, so while one
 * of them is running the previous listed command keeps the highlight — the
 * called command stays in force until the next is called.
 */
private fun listedCommandAtOrBefore(timedCommands: List<Command>, index: Int): Command {
    for (i in index downTo 0) {
        val command = timedCommands[i]
        if (command.listed) return command
    }
    return timedCommands.first()
}

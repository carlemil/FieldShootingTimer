package se.kjellstrand.fieldshootingtimer.ui

import se.kjellstrand.fieldshootingtimer.domain.COMPETITION_ALL_READY_REMAINING_SECONDS
import se.kjellstrand.fieldshootingtimer.domain.Command
import se.kjellstrand.fieldshootingtimer.domain.TimerMode
import se.kjellstrand.fieldshootingtimer.domain.timedCommandsFor

/**
 * The command to highlight in the command list.
 *
 * Competition mode owns the pre-sequence phase: "Ladda!" from before the
 * start through most of the countdown (negative [currentTime]), handing over
 * to "Alla klara!" for the final [COMPETITION_ALL_READY_REMAINING_SECONDS].
 * From 0 onward — and always in training mode — the highlight follows the
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
    parkedBySeek: Boolean = false
): Command {
    // Parked at 0 behind the "Alla klara!" dialog — conceptually still
    // in the AllReady phase.
    if (awaitingReadyConfirmation) return Command.AllReady
    if (mode == TimerMode.Competition) {
        // Only an untouched timer (still at 0) reads as "before the start" —
        // a timer parked at 0 by tapping "10 sekunder kvar!" (or scrubbing
        // the hand there) follows its parked time like any other seek.
        if (runningState == TimerRunningState.NotStarted && currentTime == 0f && !parkedBySeek) {
            return Command.Load
        }
        if (currentTime < -COMPETITION_ALL_READY_REMAINING_SECONDS) return Command.Load
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

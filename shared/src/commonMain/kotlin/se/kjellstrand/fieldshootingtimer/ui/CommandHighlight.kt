package se.kjellstrand.fieldshootingtimer.ui

import se.kjellstrand.fieldshootingtimer.domain.COMPETITION_ALL_READY_REMAINING_SECONDS
import se.kjellstrand.fieldshootingtimer.domain.Command
import se.kjellstrand.fieldshootingtimer.domain.TimerMode

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
    segmentDurations: List<Float>
): Command {
    if (mode == TimerMode.Competition) {
        if (runningState == TimerRunningState.NotStarted) return Command.Load
        if (currentTime < -COMPETITION_ALL_READY_REMAINING_SECONDS) return Command.Load
        if (currentTime < 0f) return Command.AllReady
    }
    var accumulatedTime = 0f
    segmentDurations.forEachIndexed { index, duration ->
        accumulatedTime += duration
        if (currentTime < accumulatedTime) {
            return Command.timedCommands[index]
        }
    }
    return Command.timedCommands.last()
}

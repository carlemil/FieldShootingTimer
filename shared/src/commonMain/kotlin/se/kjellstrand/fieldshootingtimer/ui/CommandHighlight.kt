package se.kjellstrand.fieldshootingtimer.ui

import se.kjellstrand.fieldshootingtimer.domain.Command

/**
 * Index into [Command.entries] of the command to highlight in the command
 * list at [currentTime]. Maps the running segment back through
 * [Command.timedCommands], so reordering the enum can't silently shift the
 * highlight. Past the last boundary the final timed command stays lit.
 */
internal fun calculateHighlightedIndex(currentTime: Float, segmentDurations: List<Float>): Int {
    var accumulatedTime = 0f
    segmentDurations.forEachIndexed { index, duration ->
        accumulatedTime += duration
        if (currentTime < accumulatedTime) {
            return Command.entries.indexOf(Command.timedCommands[index])
        }
    }
    return Command.entries.indexOf(Command.timedCommands.last())
}

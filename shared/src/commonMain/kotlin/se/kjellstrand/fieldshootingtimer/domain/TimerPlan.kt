package se.kjellstrand.fieldshootingtimer.domain

import se.kjellstrand.fieldshootingtimer.ui.Command
import kotlin.math.roundToInt

/**
 * Pure timer-plan math, derived entirely from [Command.timedCommands] and the
 * user-configurable Fire duration. Kept free of Compose and coroutines so the
 * whole schedule is verifiable with plain unit tests on any target.
 */

/** Second at which the Fire segment starts (end of the pre-fire commands). */
internal fun fireStartSeconds(): Float =
    (Command.TenSecondsLeft.duration + Command.Ready.duration).toFloat()

internal fun buildSegmentDurations(shootingDuration: Float): List<Float> =
    Command.timedCommands.map {
        if (it == Command.Fire) shootingDuration else it.duration.toFloat()
    }

/**
 * Each timed command paired with the second it starts at. Cue times are the
 * cumulative segment boundaries, so cues and dial segments can never drift
 * apart — even for fractional Fire durations.
 */
internal fun buildAudioCues(shootingDuration: Float): List<Pair<Float, Command>> {
    val durations = buildSegmentDurations(shootingDuration)
    var time = 0f
    return Command.timedCommands.mapIndexed { index, command ->
        val cue = time to command
        time += durations[index]
        cue
    }
}

internal fun buildRange(shootingDuration: Float): IntRange {
    val offset = Command.TenSecondsLeft.duration + Command.Ready.duration
    return IntRange(
        offset + 1,
        (shootingDuration + offset + Command.CeaseFire.duration - 1).toInt()
    )
}

/**
 * Picks the spot for a new user tick: the center of [range], or the nearest
 * free integer second scanning outward (forward first).
 */
internal fun findNextFreeThumbSpot(range: IntRange, thumbValues: List<Float>): Float {
    val center = (range.first + range.last) / 2
    val maxDistance = (range.last - range.first) / 2
    for (distance in 0..maxDistance) {
        val forward = center + distance
        val backward = center - distance
        if (forward in range && thumbValues.find { it.roundToInt() == forward } == null) {
            return forward.toFloat()
        }
        if (backward in range && thumbValues.find { it.roundToInt() == backward } == null) {
            return backward.toFloat()
        }
    }
    return center.toFloat()
}

/** Indices of cues whose time has been reached and that haven't fired yet. */
internal fun newlyPassedIndices(
    time: Float,
    cues: List<Pair<Float, Command>>,
    alreadyPlayed: Set<Int>
): List<Int> = cues.indices.filter { index ->
    time >= cues[index].first && index !in alreadyPlayed
}

/** Thumb values that [time] has reached and that haven't fired yet. */
internal fun newlyCrossedThumbs(
    time: Float,
    thumbs: List<Float>,
    alreadyCrossed: Set<Float>
): List<Float> = thumbs.filter { it <= time && it !in alreadyCrossed }

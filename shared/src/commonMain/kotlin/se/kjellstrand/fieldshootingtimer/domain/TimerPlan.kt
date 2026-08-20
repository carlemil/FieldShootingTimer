package se.kjellstrand.fieldshootingtimer.domain

import kotlin.math.roundToInt

/**
 * Pure timer-plan math, derived entirely from [Command.timedCommands], the
 * [TimerMode], and the user-configurable Fire duration. Kept free of Compose
 * and coroutines so the whole schedule is verifiable with plain unit tests
 * on any target.
 */

/** Competition-mode preparation countdown, run as currentTime -60..0. */
internal const val COMPETITION_COUNTDOWN_SECONDS = 60f

/** With this many countdown seconds remaining, "Alla klara!" takes the highlight. */
internal const val COMPETITION_ALL_READY_REMAINING_SECONDS = 10f

/** Second at which the Fire segment starts (end of the pre-fire commands). */
internal fun fireStartSeconds(): Float =
    (Command.TenSecondsLeft.duration + Command.Ready.duration).toFloat()

/**
 * The cease-fire beep leads the yellow segment's end slightly, compensating
 * for tick granularity and audio latency so the signal lands on the boundary.
 */
internal const val CEASE_FIRE_BEEP_LEAD_SECONDS = 0.1f

/** When the cease-fire beep fires: the yellow segment's end minus the lead. */
internal fun beepTimeSeconds(shootingDuration: Float): Float =
    fireStartSeconds() + shootingDuration + Command.CeaseFire.duration -
        CEASE_FIRE_BEEP_LEAD_SECONDS

/**
 * The immovable boundary flags shown as soon as the user has placed at least
 * one flag of their own: the start of Fire (the gray→green boundary) and the
 * end of CeaseFire (the end of the yellow segment — the dial's end). They
 * look and behave like user flags except they can't be dragged.
 */
internal fun boundaryFlagSeconds(userTicks: List<Float>, shootingDuration: Float): List<Float> =
    if (userTicks.isEmpty()) emptyList()
    else listOf(
        fireStartSeconds(),
        fireStartSeconds() + shootingDuration + Command.CeaseFire.duration
    )

/**
 * The timed sequence for [mode]: training ends after UnloadWeapon —
 * Visitation (and its pacing delay) are competition-only.
 */
internal fun timedCommandsFor(mode: TimerMode): List<Command> = when (mode) {
    TimerMode.Competition -> Command.timedCommands
    TimerMode.Training ->
        Command.timedCommands - Command.VisitationDelay - Command.Visitation
}

internal fun buildSegmentDurations(shootingDuration: Float, mode: TimerMode): List<Float> =
    timedCommandsFor(mode).map {
        if (it == Command.Fire) shootingDuration else it.duration.toFloat()
    }

/**
 * Each timed command paired with the second it starts at. Cue times are the
 * cumulative segment boundaries, so cues and dial segments can never drift
 * apart — even for fractional Fire durations.
 */
internal fun buildAudioCues(shootingDuration: Float, mode: TimerMode): List<Pair<Float, Command>> {
    val durations = buildSegmentDurations(shootingDuration, mode)
    var time = 0f
    return timedCommandsFor(mode).mapIndexed { index, command ->
        val cue = time to command
        time += durations[index]
        cue
    }
}

/**
 * The competition preparation calls, timed on the countdown's negative
 * clock: "Ladda!" as the countdown starts and "Alla klara!" with
 * [COMPETITION_ALL_READY_REMAINING_SECONDS] left. Training mode never
 * includes these — its runs start at 0.
 */
internal fun buildCompetitionPrepCues(): List<Pair<Float, Command>> = listOf(
    -COMPETITION_COUNTDOWN_SECONDS to Command.Load,
    -COMPETITION_ALL_READY_REMAINING_SECONDS to Command.AllReady
)

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

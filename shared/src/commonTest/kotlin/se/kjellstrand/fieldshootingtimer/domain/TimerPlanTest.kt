package se.kjellstrand.fieldshootingtimer.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TimerPlanTest {

    @Test
    fun `segment durations mirror the mode's timed commands with Fire replaced`() {
        // Competition runs the full sequence including the silent pacing
        // delays (3s before UnloadWeapon, 2s before Visitation)...
        assertEquals(
            listOf(7f, 3f, 5f, 3f, 3f, 4f, 2f, 2f),
            buildSegmentDurations(5f, TimerMode.Competition)
        )
        assertEquals(
            listOf(7f, 3f, 2.5f, 3f, 3f, 4f, 2f, 2f),
            buildSegmentDurations(2.5f, TimerMode.Competition)
        )
        // ...while training ends after UnloadWeapon.
        assertEquals(
            listOf(7f, 3f, 5f, 3f, 3f, 4f),
            buildSegmentDurations(5f, TimerMode.Training)
        )
    }

    @Test
    fun `training's timed sequence drops the Visitation stretch`() {
        assertEquals(Command.timedCommands, timedCommandsFor(TimerMode.Competition))
        assertEquals(
            Command.timedCommands - Command.VisitationDelay - Command.Visitation,
            timedCommandsFor(TimerMode.Training)
        )
    }

    @Test
    fun `beep time leads the yellow segment's end by a tenth of a second`() {
        // yellow end with shooting=5: 7 + 3 + 5 + 3 = 18
        assertEquals(17.9f, beepTimeSeconds(5f), 1e-4f)
        assertEquals(15.4f, beepTimeSeconds(2.5f), 1e-4f)
    }

    @Test
    fun `cue times equal cumulative segment boundaries for integer and fractional durations`() {
        for (mode in TimerMode.entries) {
            for (shooting in listOf(0f, 2f, 2.5f, 5f, 7.75f, 20f)) {
                val durations = buildSegmentDurations(shooting, mode)
                val cues = buildAudioCues(shooting, mode)
                var boundary = 0f
                durations.forEachIndexed { index, duration ->
                    assertEquals(
                        boundary, cues[index].first, 1e-4f,
                        "cue $index for shooting=$shooting/$mode should start at its segment boundary"
                    )
                    boundary += duration
                }
            }
        }
    }

    @Test
    fun `cues follow the mode's timed command sequence`() {
        assertEquals(
            Command.timedCommands,
            buildAudioCues(5f, TimerMode.Competition).map { it.second }
        )
        assertEquals(
            timedCommandsFor(TimerMode.Training),
            buildAudioCues(5f, TimerMode.Training).map { it.second }
        )
    }

    @Test
    fun `competition prep cues call Load and AllReady on the countdown clock`() {
        // 60s Ladda phase + 10s Alla klara wait = the -70..0 countdown.
        assertEquals(
            listOf(-70f to Command.Load, -10f to Command.AllReady),
            buildCompetitionPrepCues()
        )
    }

    @Test
    fun `range starts after the pre-fire commands and ends before Visitation`() {
        // offset = TenSecondsLeft(7) + Ready(3) = 10
        assertEquals(11..17, buildRange(5f))
        assertEquals(11..12, buildRange(0f))
    }

    @Test
    fun `boundary flags are absent without user ticks`() {
        assertEquals(emptyList(), boundaryFlagSeconds(emptyList(), 5f))
    }

    @Test
    fun `boundary flags mark the Fire start and the dial end once a user tick exists`() {
        // fireStart = TenSecondsLeft(7) + Ready(3) = 10;
        // dial end = fireStart + Fire(5) + CeaseFire(3) = 18
        assertEquals(listOf(10f, 18f), boundaryFlagSeconds(listOf(12f), 5f))
        // The dial end follows the configurable Fire duration.
        assertEquals(listOf(10f, 15.5f), boundaryFlagSeconds(listOf(11f, 12f), 2.5f))
    }

    @Test
    fun `findNextFreeThumbSpot picks the center of an empty range`() {
        assertEquals(8f, findNextFreeThumbSpot(4..12, emptyList()))
    }

    @Test
    fun `findNextFreeThumbSpot scans outward forward first`() {
        assertEquals(9f, findNextFreeThumbSpot(4..12, listOf(8f)))
        assertEquals(7f, findNextFreeThumbSpot(4..12, listOf(8f, 9f)))
    }

    @Test
    fun `findNextFreeThumbSpot falls back to center when the range is full`() {
        val full = (4..12).map { it.toFloat() }
        assertEquals(8f, findNextFreeThumbSpot(4..12, full))
    }

    @Test
    fun `newlyPassedIndices includes a cue exactly at the current time`() {
        val cues = buildAudioCues(5f, TimerMode.Training)
        // cue 1 (Ready) fires at t=7
        assertEquals(listOf(0, 1), newlyPassedIndices(7f, cues, emptySet()))
    }

    @Test
    fun `newlyPassedIndices is idempotent once indices are recorded`() {
        val cues = buildAudioCues(5f, TimerMode.Training)
        val fired = newlyPassedIndices(8f, cues, emptySet()).toSet()
        assertTrue(newlyPassedIndices(8f, cues, fired).isEmpty())
    }

    @Test
    fun `newlyCrossedThumbs includes a thumb exactly at the current time and is idempotent`() {
        val thumbs = listOf(11f, 13f)
        assertEquals(listOf(11f), newlyCrossedThumbs(11f, thumbs, emptySet()))
        assertTrue(newlyCrossedThumbs(11f, thumbs, setOf(11f)).isEmpty())
        assertEquals(listOf(13f), newlyCrossedThumbs(13.5f, thumbs, setOf(11f)))
    }
}

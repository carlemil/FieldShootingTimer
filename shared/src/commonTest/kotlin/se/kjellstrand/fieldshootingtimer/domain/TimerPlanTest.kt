package se.kjellstrand.fieldshootingtimer.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TimerPlanTest {

    @Test
    fun `segment durations mirror timedCommands with Fire replaced`() {
        assertEquals(listOf(7f, 3f, 5f, 3f, 4f, 2f), buildSegmentDurations(5f))
        assertEquals(listOf(7f, 3f, 2.5f, 3f, 4f, 2f), buildSegmentDurations(2.5f))
    }

    @Test
    fun `cue times equal cumulative segment boundaries for integer and fractional durations`() {
        for (shooting in listOf(0f, 2f, 2.5f, 5f, 7.75f, 20f)) {
            val durations = buildSegmentDurations(shooting)
            val cues = buildAudioCues(shooting)
            var boundary = 0f
            durations.forEachIndexed { index, duration ->
                assertEquals(
                    boundary, cues[index].first, 1e-4f,
                    "cue $index for shooting=$shooting should start at its segment boundary"
                )
                boundary += duration
            }
        }
    }

    @Test
    fun `cues follow the timedCommands sequence`() {
        assertEquals(Command.timedCommands, buildAudioCues(5f).map { it.second })
    }

    @Test
    fun `range starts after the pre-fire commands and ends before Visitation`() {
        // offset = TenSecondsLeft(7) + Ready(3) = 10
        assertEquals(11..17, buildRange(5f))
        assertEquals(11..12, buildRange(0f))
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
        val cues = buildAudioCues(5f)
        // cue 1 (Ready) fires at t=7
        assertEquals(listOf(0, 1), newlyPassedIndices(7f, cues, emptySet()))
    }

    @Test
    fun `newlyPassedIndices is idempotent once indices are recorded`() {
        val cues = buildAudioCues(5f)
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

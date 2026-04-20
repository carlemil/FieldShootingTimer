package se.kjellstrand.fieldshootingtimer.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class TickBadgePlacementsTest {

    private val fireStart = 10f   // TenSecondsLeft (7) + Ready (3)
    private val unloadStart = 18f // fireStart + Fire (5) + CeaseFire (3)
    private val tolerance = 0.0001f

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `single tick produces two placements bracketed by fireStart and unloadStart`() {
        val result = tickBadgePlacements(listOf(15f), fireStart, unloadStart)
        assertEquals(2, result.size)
        assertEquals(12.5f, result[0].first, tolerance)
        assertEquals(5, result[0].second)
        assertEquals(16.5f, result[1].first, tolerance)
        assertEquals(3, result[1].second)
    }

    @Test
    fun `multiple ticks produce N plus 1 midpoint placements`() {
        val result = tickBadgePlacements(listOf(12f, 15f), fireStart, unloadStart)
        assertEquals(3, result.size)
        assertEquals(11f, result[0].first, tolerance)
        assertEquals(2, result[0].second)
        assertEquals(13.5f, result[1].first, tolerance)
        assertEquals(3, result[1].second)
        assertEquals(16.5f, result[2].first, tolerance)
        assertEquals(3, result[2].second)
    }

    @Test
    fun `unsorted input is sorted before producing placements`() {
        val expected = tickBadgePlacements(listOf(12f, 15f), fireStart, unloadStart)
        val actual = tickBadgePlacements(listOf(15f, 12f), fireStart, unloadStart)
        assertEquals(expected.size, actual.size)
        expected.zip(actual).forEach { (e, a) ->
            assertEquals(e.first, a.first, tolerance)
            assertEquals(e.second, a.second)
        }
    }

    @Test
    fun `fractional ticks round to the nearest integer for labels`() {
        // boundaries: 10.0, 11.4, 12.6, 18.0
        // deltas: 1.4 -> 1, 1.2 -> 1, 5.4 -> 5
        // midpoints: 10.7, 12.0, 15.3
        // Values picked to stay away from the 0.5 Float-rounding boundary.
        val result = tickBadgePlacements(listOf(11.4f, 12.6f), fireStart, unloadStart)
        assertEquals(3, result.size)
        assertEquals(10.7f, result[0].first, tolerance)
        assertEquals(1, result[0].second)
        assertEquals(12.0f, result[1].first, tolerance)
        assertEquals(1, result[1].second)
        assertEquals(15.3f, result[2].first, tolerance)
        assertEquals(5, result[2].second)
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `empty tick list produces empty placement list`() {
        assertEquals(emptyList<Pair<Float, Int>>(), tickBadgePlacements(emptyList(), fireStart, unloadStart))
    }
}

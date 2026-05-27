package se.kjellstrand.fieldshootingtimer.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class TickBadgePlacementsTest {

    private val fireStart = 10f
    private val unloadStart = 18f
    private val tolerance = 0.0001f

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
        val result = tickBadgePlacements(listOf(11.4f, 12.6f), fireStart, unloadStart)
        assertEquals(3, result.size)
        assertEquals(10.7f, result[0].first, tolerance)
        assertEquals(1, result[0].second)
        assertEquals(12.0f, result[1].first, tolerance)
        assertEquals(1, result[1].second)
        assertEquals(15.3f, result[2].first, tolerance)
        assertEquals(5, result[2].second)
    }

    @Test
    fun `empty tick list produces empty placement list`() {
        assertEquals(emptyList(), tickBadgePlacements(emptyList(), fireStart, unloadStart))
    }
}

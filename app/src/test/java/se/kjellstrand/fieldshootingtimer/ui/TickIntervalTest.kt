package se.kjellstrand.fieldshootingtimer.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class TickIntervalTest {

    private val fireStart = 10f // TenSecondsLeft (7) + Ready (3)

    @Test
    fun `empty tick list produces empty label list`() {
        assertEquals(emptyList<Pair<Float, Int>>(), tickIntervalLabels(emptyList(), fireStart))
    }

    @Test
    fun `single tick is labeled with delta from fire start`() {
        assertEquals(listOf(11f to 1), tickIntervalLabels(listOf(11f), fireStart))
    }

    @Test
    fun `consecutive ticks are labeled with deltas between them`() {
        // first delta measured from fireStart (10): 11-10, 13-11, 15-13
        assertEquals(
            listOf(11f to 1, 13f to 2, 15f to 2),
            tickIntervalLabels(listOf(11f, 13f, 15f), fireStart)
        )
    }

    @Test
    fun `unsorted input is sorted before computing deltas`() {
        assertEquals(
            listOf(11f to 1, 13f to 2, 15f to 2),
            tickIntervalLabels(listOf(15f, 11f, 13f), fireStart)
        )
    }

    @Test
    fun `fractional ticks round to the nearest integer`() {
        // 11.4 - 10 = 1.4 → 1; 12.6 - 11.4 = 1.2 → 1; 14.3 - 12.6 = 1.7 → 2
        // Values avoid the 0.5 rounding boundary where Float arithmetic is unstable.
        assertEquals(
            listOf(11.4f to 1, 12.6f to 1, 14.3f to 2),
            tickIntervalLabels(listOf(11.4f, 12.6f, 14.3f), fireStart)
        )
    }
}

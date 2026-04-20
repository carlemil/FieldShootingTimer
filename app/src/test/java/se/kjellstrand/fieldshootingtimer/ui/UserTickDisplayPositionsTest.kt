package se.kjellstrand.fieldshootingtimer.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class UserTickDisplayPositionsTest {

    private val fireStart = 10f
    private val unloadStart = 18f

    @Test
    fun `empty input produces empty output`() {
        assertEquals(
            emptyList<Float>(),
            userTickDisplayPositions(emptyList(), fireStart, unloadStart)
        )
    }

    @Test
    fun `single tick is bracketed by fireStart and unloadStart`() {
        assertEquals(
            listOf(10f, 12f, 18f),
            userTickDisplayPositions(listOf(12f), fireStart, unloadStart)
        )
    }

    @Test
    fun `multiple ticks preserve order with fireStart prepended and unloadStart appended`() {
        assertEquals(
            listOf(10f, 11f, 13f, 15f, 18f),
            userTickDisplayPositions(listOf(11f, 13f, 15f), fireStart, unloadStart)
        )
    }

    @Test
    fun `boundaries are added verbatim even when equal to an existing tick`() {
        // Caller controls dedup if needed; the helper simply wraps.
        assertEquals(
            listOf(10f, 10f, 18f, 18f),
            userTickDisplayPositions(listOf(10f, 18f), fireStart, unloadStart)
        )
    }
}

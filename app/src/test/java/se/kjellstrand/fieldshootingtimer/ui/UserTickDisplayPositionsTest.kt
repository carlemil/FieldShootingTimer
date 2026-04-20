package se.kjellstrand.fieldshootingtimer.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class UserTickDisplayPositionsTest {

    private val unloadStart = 18f

    @Test
    fun `empty input produces empty output`() {
        assertEquals(emptyList<Float>(), userTickDisplayPositions(emptyList(), unloadStart))
    }

    @Test
    fun `single tick is followed by unloadStart`() {
        assertEquals(listOf(12f, 18f), userTickDisplayPositions(listOf(12f), unloadStart))
    }

    @Test
    fun `multiple ticks preserve order and unloadStart is appended once`() {
        assertEquals(
            listOf(11f, 13f, 15f, 18f),
            userTickDisplayPositions(listOf(11f, 13f, 15f), unloadStart)
        )
    }

    @Test
    fun `unloadStart is appended verbatim even when equal to an existing tick`() {
        // Caller controls dedup if needed; the helper simply appends.
        assertEquals(
            listOf(18f, 18f),
            userTickDisplayPositions(listOf(18f), unloadStart)
        )
    }
}

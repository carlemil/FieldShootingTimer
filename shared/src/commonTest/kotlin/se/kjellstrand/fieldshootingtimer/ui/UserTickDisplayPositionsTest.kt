package se.kjellstrand.fieldshootingtimer.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class UserTickDisplayPositionsTest {

    private val fireStart = 10f
    private val unloadStart = 18f

    @Test
    fun `empty input produces empty output`() {
        assertEquals(
            emptyList(),
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
        assertEquals(
            listOf(10f, 10f, 18f, 18f),
            userTickDisplayPositions(listOf(10f, 18f), fireStart, unloadStart)
        )
    }
}

package se.kjellstrand.fieldshootingtimer.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CountdownDisplayTest {

    @Test
    fun `the Ladda phase counts its own sixty seconds`() {
        // -70..-10 is the Ladda stretch: digits 60 → 1.
        assertEquals(60, countdownSecondsOrNull(-70f))
        assertEquals(33, countdownSecondsOrNull(-42.3f))
        assertEquals(1, countdownSecondsOrNull(-10.1f))
    }

    @Test
    fun `the Alla klara wait counts its final ten seconds`() {
        assertEquals(10, countdownSecondsOrNull(-10f))
        assertEquals(5, countdownSecondsOrNull(-4.3f))
        assertEquals(1, countdownSecondsOrNull(-0.1f))
    }

    @Test
    fun `the repeated wait counts its full span`() {
        assertEquals(15, countdownSecondsOrNull(-15f, allReadyRepeat = true))
        assertEquals(12, countdownSecondsOrNull(-11.5f, allReadyRepeat = true))
        assertEquals(1, countdownSecondsOrNull(-0.1f, allReadyRepeat = true))
    }

    @Test
    fun `no digits at or after zero`() {
        assertNull(countdownSecondsOrNull(0f))
        assertNull(countdownSecondsOrNull(12f))
        assertNull(countdownSecondsOrNull(0f, allReadyRepeat = true))
    }
}

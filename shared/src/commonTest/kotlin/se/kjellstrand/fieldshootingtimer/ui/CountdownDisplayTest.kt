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

    // The shooting stretch is green + yellow: Fire starts at 10s (7 + 3) and
    // CeaseFire adds 3s, so a 5s fire time spans 10..18 — a total of 8.

    @Test
    fun `the shooting stretch counts green plus yellow down`() {
        assertEquals(8, shootingSecondsRemainingOrNull(10f, 5f))
        assertEquals(5, shootingSecondsRemainingOrNull(13f, 5f))
        assertEquals(1, shootingSecondsRemainingOrNull(17.9f, 5f))
    }

    @Test
    fun `before the stretch it reads the full total`() {
        // Parked, mid gray lead-in, and back in the competition countdown.
        assertEquals(8, shootingSecondsRemainingOrNull(0f, 5f))
        assertEquals(8, shootingSecondsRemainingOrNull(7f, 5f))
        assertEquals(8, shootingSecondsRemainingOrNull(-70f, 5f))
    }

    @Test
    fun `a longer fire time raises the total`() {
        assertEquals(63, shootingSecondsRemainingOrNull(0f, 60f))
        assertEquals(9, shootingSecondsRemainingOrNull(0f, 5.5f))
    }

    @Test
    fun `no digits once the yellow segment ends`() {
        assertNull(shootingSecondsRemainingOrNull(18f, 5f))
        assertNull(shootingSecondsRemainingOrNull(25f, 5f))
    }
}

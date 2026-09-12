package se.kjellstrand.fieldshootingtimer.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CountdownDisplayTest {

    @Test
    fun `the gap before the sequence counts down`() {
        assertEquals(3, countdownSecondsOrNull(-3f))
        assertEquals(2, countdownSecondsOrNull(-1.5f))
        assertEquals(1, countdownSecondsOrNull(-0.1f))
    }

    @Test
    fun `no digits at or after zero`() {
        assertNull(countdownSecondsOrNull(0f))
        assertNull(countdownSecondsOrNull(12f))
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
        // Parked, mid gray lead-in, and back in the competition gap.
        assertEquals(8, shootingSecondsRemainingOrNull(0f, 5f))
        assertEquals(8, shootingSecondsRemainingOrNull(7f, 5f))
        assertEquals(8, shootingSecondsRemainingOrNull(-3f, 5f))
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

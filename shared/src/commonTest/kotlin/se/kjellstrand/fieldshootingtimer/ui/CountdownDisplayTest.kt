package se.kjellstrand.fieldshootingtimer.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CountdownDisplayTest {

    @Test
    fun `negative time rounds up to whole remaining seconds`() {
        assertEquals(43, countdownSecondsOrNull(-42.3f))
        assertEquals(60, countdownSecondsOrNull(-60f))
        assertEquals(1, countdownSecondsOrNull(-0.1f))
    }

    @Test
    fun `non-negative time shows no countdown`() {
        assertNull(countdownSecondsOrNull(0f))
        assertNull(countdownSecondsOrNull(12f))
    }
}

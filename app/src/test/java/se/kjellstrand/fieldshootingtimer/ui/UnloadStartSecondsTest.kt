package se.kjellstrand.fieldshootingtimer.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class UnloadStartSecondsTest {

    private val tolerance = 0.0001f

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `unloadStart for a 5 second fire equals 18`() {
        // TenSecondsLeft (7) + Ready (3) + Fire (5) + CeaseFire (3) = 18
        assertEquals(18f, unloadStartSeconds(5f), tolerance)
    }

    @Test
    fun `unloadStart for a 10 second fire equals 23`() {
        // 7 + 3 + 10 + 3 = 23
        assertEquals(23f, unloadStartSeconds(10f), tolerance)
    }

    @Test
    fun `unloadStart for a zero length fire equals prelude plus ceaseFire`() {
        // 7 + 3 + 0 + 3 = 13
        assertEquals(13f, unloadStartSeconds(0f), tolerance)
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `unloadStart shifts linearly with fireDuration`() {
        // Two evaluations differ by exactly the fireDuration delta.
        assertEquals(3f, unloadStartSeconds(8f) - unloadStartSeconds(5f), tolerance)
    }
}

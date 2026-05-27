package se.kjellstrand.fieldshootingtimer.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class UnloadStartSecondsTest {

    private val tolerance = 0.0001f

    @Test
    fun `unloadStart for a 5 second fire equals 18`() {
        assertEquals(18f, unloadStartSeconds(5f), tolerance)
    }

    @Test
    fun `unloadStart for a 10 second fire equals 23`() {
        assertEquals(23f, unloadStartSeconds(10f), tolerance)
    }

    @Test
    fun `unloadStart for a zero length fire equals prelude plus ceaseFire`() {
        assertEquals(13f, unloadStartSeconds(0f), tolerance)
    }

    @Test
    fun `unloadStart shifts linearly with fireDuration`() {
        assertEquals(3f, unloadStartSeconds(8f) - unloadStartSeconds(5f), tolerance)
    }
}

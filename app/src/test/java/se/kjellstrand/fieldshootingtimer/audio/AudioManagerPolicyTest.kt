package se.kjellstrand.fieldshootingtimer.audio

import android.media.AudioManager.RINGER_MODE_NORMAL
import android.media.AudioManager.RINGER_MODE_SILENT
import android.media.AudioManager.RINGER_MODE_VIBRATE
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AudioManagerPolicyTest {

    // --- Fixed behavior (should FAIL before fix, PASS after fix) ---

    @Test
    fun `shouldPlayAudio returns true for RINGER_MODE_NORMAL`() {
        assertTrue(shouldPlayAudio(RINGER_MODE_NORMAL))
    }

    @Test
    fun `shouldPlayAudio returns false for RINGER_MODE_SILENT`() {
        assertFalse(shouldPlayAudio(RINGER_MODE_SILENT))
    }

    @Test
    fun `shouldPlayAudio returns false for RINGER_MODE_VIBRATE`() {
        assertFalse(shouldPlayAudio(RINGER_MODE_VIBRATE))
    }

    @Test
    fun `shouldPlayAudio returns false for any non-NORMAL mode`() {
        // If Android adds a new ringer mode in the future, audio must not play
        // unless the mode is explicitly RINGER_MODE_NORMAL.
        val unknownMode = 99
        assertFalse(shouldPlayAudio(unknownMode))
    }

    // --- Guard tests (should PASS before and after fix) ---

    @Test
    fun `RINGER_MODE constants are the documented Android values`() {
        // Guard against any future Android SDK shuffle of these int constants;
        // the policy depends on these specific values.
        assertTrue(RINGER_MODE_SILENT == 0)
        assertTrue(RINGER_MODE_VIBRATE == 1)
        assertTrue(RINGER_MODE_NORMAL == 2)
    }
}

package se.kjellstrand.fieldshootingtimer

import se.kjellstrand.fieldshootingtimer.domain.Command
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CuePlaybackTest {

    @Test
    fun `voice mode plays every audible cue`() {
        Command.timedCommands.filter { it.audioPath != null }.forEach { command ->
            assertTrue(
                shouldPlayCueVoice(command, ceaseFireBeep = false),
                "voice mode should play $command"
            )
        }
    }

    @Test
    fun `silent pacing delays never play a voice`() {
        listOf(Command.UnloadWeaponDelay, Command.VisitationDelay).forEach { command ->
            assertFalse(shouldPlayCueVoice(command, ceaseFireBeep = false))
            assertFalse(shouldPlayCueVoice(command, ceaseFireBeep = true))
        }
    }

    @Test
    fun `beep mode silences the CeaseFire voice`() {
        assertFalse(shouldPlayCueVoice(Command.CeaseFire, ceaseFireBeep = true))
        assertTrue(shouldPlayCueVoice(Command.CeaseFire, ceaseFireBeep = false))
    }

    @Test
    fun `beep mode leaves all other audible cues untouched`() {
        listOf(
            Command.TenSecondsLeft,
            Command.Ready,
            Command.Fire,
            Command.UnloadWeapon,
            Command.Visitation,
            Command.Mark
        ).forEach { command ->
            assertTrue(
                shouldPlayCueVoice(command, ceaseFireBeep = true),
                "beep mode should not affect $command"
            )
        }
    }
}
